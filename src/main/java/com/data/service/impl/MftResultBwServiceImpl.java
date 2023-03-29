package com.data.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.data.entity.*;
import com.data.mapper.*;
import com.data.service.MftResultBwService;
import com.data.util.MftSendUtil;
import com.data.util.PublicUtil;
import com.data.util.Sm4Util;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CountDownLatch;


/**
 * @Description: 舱单回执报文
 * @Author: xuyang
 * @Date: 2022/9/13  14:08
 */
@Service("mftResultBwService")
@Slf4j
@Transactional(rollbackFor = {Exception.class})
public class MftResultBwServiceImpl implements MftResultBwService {

    @Value("${msgReceiptAnalysisPath}")
    private String msgReceiptAnalysisPath;

    @Value("${msgReceiptBakPath}")
    private String msgReceiptBakPath;

    @Value("${msgReceiptNoDeclarePath}")
    private String msgReceiptNoDeclarePath;

    @Value("${msgResultErrorDir}")
    private String msgResultErrorDir;

    @Value("${kToDzPath}")
    private String kToDzPath;

    @Value("${kToHaiguanPath}")
    private String kToHaiguanPath;

    @Value("${sToDzPath}")
    private String sToDzPath;

    @Value("${sToHaiguanPath}")
    private String sToHaiguanPath;

    @Autowired(required = false)
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired(required = false)
    private BaseResultXmlMapper baseResultXmlMapper;

    @Autowired(required = false)
    private BaseXmlMapper baseXmlMapper;

    @Autowired(required = false)
    private BdUserMapper bdUserMapper;

    @Autowired(required = false)
    private BaseUserMapper baseUserMapper;

    @Autowired(required = false)
    private BaseDeptMapper baseDeptMapper;

    @Value("${SM4Key}")
    private String SM4Key;

    /**
     * 空运报文类型
     */
    private List<String> airTypeList = Arrays.asList("MT3201", "MT5201", "MT5202");

    /**
     * 水运报文类型
     */
    private List<String> seaTypeList = Arrays.asList("MT3101", "MT5101", "MT5102");

    /**
     * @Description: 处理回执
     * @Author: xuyang
     * @Date: 2022/9/13  14:09
     */
    @Override
    @Async("taskExecutor")
    public void parse(List<File> fileList, CountDownLatch latch) {
        // 舱单报文开始改造
        int num = 1;
        for (File file : fileList) {
            // 读取xml文件内容
            String xmlStr = PublicUtil.readFile(file);
            Map<String, Object> map = new ListOrderedMap();
            try {
                map = PublicUtil.xmlToMap(xmlStr);
            } catch (DocumentException e) {
                e.printStackTrace();
                log.error("报文转换出错：" + e.getMessage());
                PublicUtil.moveFile(file, msgResultErrorDir, "ERROR");
                continue;
            }
            this.parseBw(map, file, num);
            num++;

        }
        latch.countDown();
    }

    private void parseBw(Map<String, Object> map, File file, int num) {


        //获取Head节点下的MessageID、FunctionCode、MessageType、SendTime的值
        String messageId = null;
        String functionCode = null;
        String messageType = null;
        String sendTime = null;
        String receiverId = null;
        //获得报文的类型 根节点为PlatImpRet的是1（响应回执） 根节点为Manifest是2（业务回执） 文件以Failed开头是3
        String xmlType = null;
        String failInfo = null;
        Map headMap = null;
        String kafkaMsg = null;
//            String messageStatus = null;
        //回执报文信息
        String content = null;
        try {
            content = PublicUtil.readFile(file);
            if (!ObjectUtils.isEmpty(map.get("PlatImpRet"))) {
                Map platImpRetMap = (Map) map.get("PlatImpRet");
                Map retBizDataMap = (Map) platImpRetMap.get("RetBizData");
                Map manifestMap = (Map) retBizDataMap.get("Manifest");
                headMap = (Map) manifestMap.get("Head");
                xmlType = "1";
//                messageStatus = "0";
                kafkaMsg = PublicUtil.getXmlAttrValue(content, "PlatImpRet.RetBizData.Manifest");
                //格式化美化xml
                kafkaMsg = PublicUtil.beautifyXml(kafkaMsg);
            } else if (!ObjectUtils.isEmpty(map.get("Manifest"))) {
                Map manifestMap = (Map) map.get("Manifest");
                headMap = (Map) manifestMap.get("Head");
                //海关回执 获取Code
//                Map responseMap = (Map) manifestMap.get("Response");
//                Map consignmentMap = (Map) responseMap.get("Consignment");
//                Map responseTypeMap = (Map) consignmentMap.get("ResponseType");
//                messageStatus = responseTypeMap.get("Code") == null ? messageStatus : responseTypeMap.get("Code").toString();
//                if ("01".equals(messageStatus)) {
//                    messageStatus = "01";
//                } else {
//                    messageStatus = "03";
//                }
                xmlType = "2";
                kafkaMsg = content;
                //处理以Fail开头的报文
            } else if (file.getName().startsWith("Failed")) {
                String[] fileName = file.getName().split("\\$");
                messageId = fileName[2].substring(1);
                xmlType = "3";
//                messageStatus = "-1";
                kafkaMsg = content;
                failInfo = StringUtils.isNotBlank(PublicUtil.getCutOutString(content, "<FailInfo>", "</FailInfo>"))
                        ? PublicUtil.getCutOutString(content, "<FailInfo>", "</FailInfo>") :
                        PublicUtil.getCutOutString(content, "<failInfo>", "</failInfo>");
            }
            messageId = headMap != null ? String.valueOf(headMap.get("MessageID")) : messageId;
            functionCode = headMap != null ? String.valueOf(headMap.get("FunctionCode")) : functionCode;
            messageType = headMap != null ? String.valueOf(headMap.get("MessageType")) : messageType;
            sendTime = headMap != null ? String.valueOf(headMap.get("SendTime")) : sendTime;
            receiverId = headMap != null ? String.valueOf(headMap.get("ReceiverID")) : receiverId;

        } catch (Exception e) {
            e.printStackTrace();
            log.error("回执报文节点获取失败：{}", e.getMessage());
            //文件迁移到备份目录的ERROR文件里
            PublicUtil.moveFile(file, msgResultErrorDir, "ERROR");
            return;
        }

        //报文回执待解析路径
        String bwResultAnalPath = msgReceiptAnalysisPath + file.getName();
        //报文回执备份目录
        String bwResultBakPath = msgReceiptBakPath + LocalDateTime.now().format(DateTimeFormatter
                .ofPattern("yyyyMMdd")) + File.separator + file.getName();
        //回执报文未申报路径
        String bwResultNoDeclarePath = msgReceiptNoDeclarePath + LocalDateTime.now().format(DateTimeFormatter
                .ofPattern("yyyyMMdd")) + File.separator + file.getName();


        String id = "";
        String copCode = "";
        //用户类型
        String userType = "";
        String accessCode = "";


        BaseResultXml baseResultXml = new BaseResultXml();
        try {
            if ("MT9999".equals(messageType)) {
                copCode = receiverId.substring(4, 13);
                // 配置表中查询该组织机构代码属于那个通道
                QueryWrapper<BaseDept> baseDeptWrapper = new QueryWrapper<>();
                baseDeptWrapper.eq("DEPT_NO", copCode);
                baseDeptWrapper.ne("ACCESS_CODE", "MA1W6AK72");
                List<BaseDept> baseDeptList = baseDeptMapper.selectList(baseDeptWrapper);
                if (baseDeptList != null && baseDeptList.size() > 0) {
                    // 平台用户
                    userType = "1";
                    accessCode = baseDeptList.get(0).getAccessCode();
                } else {
                    // 普通用户
                    userType = "0";
                }

            } else {
                //根据message_id查出舱单报文表中的copCode
                QueryWrapper<BaseXml> baseXmlQueryWrapper = new QueryWrapper<>();
                baseXmlQueryWrapper.likeLeft("MESSAGE_ID", messageId);
                List<BaseXml> baseXmlList = baseXmlMapper.selectList(baseXmlQueryWrapper);
                if (baseXmlList == null || baseXmlList.size() <= 0) {
                    //相当于报文回执移动到报文回执未申报目录下
                    PublicUtil.deleteFileOrDirectory(file.getPath());
                    PublicUtil.createFile(bwResultNoDeclarePath, content);
                    return;
                }

                id = baseXmlList.get(0).getId();
                copCode = baseXmlList.get(0).getCopCode();
                //用户类型
                userType = baseXmlList.get(0).getUserType();
                accessCode = baseXmlList.get(0).getAccessCode();

                // 为了Failed回执的messageId不是完整的，所以重新赋值为了能关联上申报单
                messageId = baseXmlList.get(0).getMessageId();
            }

            //判断是否是重发回执报文
            QueryWrapper<BaseResultXml> queryWrapper = new QueryWrapper<>();
            queryWrapper.like("MSG_PATH", file.getName());
            List<BaseResultXml> list = baseResultXmlMapper.selectList(queryWrapper);
            if (list != null && list.size() != 0) {
                baseResultXml = list.get(0);
                baseResultXml.setFlag("0");
                baseResultXml.setMsgPath(bwResultBakPath);
                baseResultXmlMapper.updateById(baseResultXml);
            } else {
                //插入数据到舱单回执报文表中
                baseResultXml.setId(UUID.randomUUID().toString().replaceAll("-", ""));
                baseResultXml.setMessageId(messageId);
                baseResultXml.setFunctionCode(functionCode);
                baseResultXml.setMessageType(messageType);
                baseResultXml.setSendTime(sendTime);
                baseResultXml.setCreateDate(LocalDateTime.now());
                baseResultXml.setMsgPath(bwResultBakPath);
                baseResultXml.setXmlType(xmlType);
                baseResultXml.setCopCode(copCode);
                baseResultXml.setFailInfo(failInfo);
                baseResultXml.setFlag("0");
                baseResultXml.setRepeatTime(0);
                baseResultXmlMapper.insert(baseResultXml);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("操作数据库失败：{}", e.getMessage());
            //文件迁移到备份目录的ERROR文件里
            PublicUtil.moveFile(file, msgResultErrorDir, "ERROR");
            return;
        }

        //判断如果是运抵理货报文类型 空运
        String result = null;
        try {
            if (airTypeList.contains(messageType) || seaTypeList.contains(messageType)) {
                // 运抵理货回执处理
                String path1 = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                        + String.format("%04d", num) + '$'
                        + id + '$' + messageId + '$' + messageId.substring(4, 9) + '$'
                        + messageType + '_' + functionCode + '$'
//                        + messageStatus
                        + ".xml";
                result = this.receive2(messageType, messageId, receiverId, content, xmlType, file, path1, bwResultNoDeclarePath, bwResultAnalPath, bwResultBakPath, baseResultXml);
            } else {
                // 原始预配回执处理
                result = this.receive1(userType, accessCode, copCode, messageType, kafkaMsg, content, file, bwResultAnalPath, bwResultBakPath, baseResultXml);
            }

            //处理回执失败
            if (result != null) {
                baseResultXml.setFlag("1");
                baseResultXml.setErrorMsg(result);
                baseResultXmlMapper.updateById(baseResultXml);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("处理回执失败：{}", e.getMessage());
            //文件迁移到备份目录的ERROR文件里
            PublicUtil.moveFile(file, msgResultErrorDir, "ERROR");
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return;
        }


    }

    /**
    　* @Description: 原始预配回执处理逻辑
    　* @Author: ps
    　* @Date: 2023/2/27 0027 17:53
    　*/
    private String receive1(String userType,String accessCode,String copCode,String messageType,String kafkaMsg,String content,
                          File file,String bwResultAnalPath,String bwResultBakPath,BaseResultXml baseResultXml){
        String deptNo = null;
        String topic = null;
        //平台用户
        if ("1".equals(userType)) {
            //将回执报文信息放到Kakfa中 topic名为用户的登记单位组织机构代码
            deptNo = accessCode;
        } else {
            //普通用户 去通道用户表查询
            deptNo = copCode;
        }

        try {
            //根据deptNo关联BD_USER表的18位社会信用代码，如果有数据 舱单回执发送topic作为topic发送报文回执 如果没数据则不推送
            List<BdUser> bdUserList = bdUserMapper.listByDeptNo(deptNo);
            if (bdUserList == null || bdUserList.size() <= 0) {
                log.info("{}登记单位组织机构代码未获取到用户信息的topic", deptNo);
                String filePath = PublicUtil.moveFile(file, msgReceiptNoDeclarePath, "NoPush/" + deptNo);
                baseResultXml.setFlag("2");
                baseResultXml.setErrorMsg("未获取到用户信息的topic");
                baseResultXml.setMsgPath(filePath);
                baseResultXmlMapper.updateById(baseResultXml);
                return null;
            }
            topic = bdUserList.get(0).getTcSd();

            //Kafka消息发送
            String finalMessageType = messageType;
            String finalFileName = file.getName();
            String encryptKafkaMsg = Sm4Util.encryptEcb(SM4Key, kafkaMsg);
            //报文推送kafka 调用公共方法 外层添加报文数据类型以便在同一topic区分
            //舱单回执
            String sendData = MftSendUtil.mftSend("mftRec", encryptKafkaMsg);
            kafkaTemplate.send(topic, file.getName().replaceAll(".xml", ""), sendData)
                    .addCallback(success -> {
                        //发送成功
                        // 消息发送到的topic
                        String topic2 = success.getRecordMetadata().topic();
                        // 消息发送到的分区
                        int partition = success.getRecordMetadata().partition();
                        // 消息在分区内的offset
                        long offset = success.getRecordMetadata().offset();
                        log.info("{}报文{}发送成功,topic:{},partition:{},offset{}", finalMessageType, finalFileName, topic2, partition, offset);
                    }, failure -> {
                        //发送失败的处理
                        log.error("{}报文{}发送失败：{}", finalMessageType, finalFileName, failure.getMessage());
                        //文件迁移到备份目录的ERROR文件里
                        String filePath = PublicUtil.moveFile(file, msgResultErrorDir, "ERROR");
                        baseResultXml.setFlag("1");
                        baseResultXml.setMsgPath(filePath);
                        return;
                    });
            if ("1".equals(baseResultXml.getFlag())) {
                return "推送失败";
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("推送失败");
            String filePath = PublicUtil.moveFile(file, msgResultErrorDir, "ERROR");
            baseResultXml.setMsgPath(filePath);
            return "推送失败";
        }

        try {
            //删除回执并创建备份回执
            PublicUtil.deleteFileOrDirectory(file.getPath());
            // 回执待解析 没有重发过的回执才进待解析目录，防止重发过后的重复解析
            if (baseResultXml.getRepeatTime() == 0) {
                PublicUtil.createFile(bwResultAnalPath, content);
            }
            //回执备份
            PublicUtil.createFile(bwResultBakPath, content);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("迁移文件失败");
            String filePath = PublicUtil.moveFile(file, msgResultErrorDir, "ERROR");
            baseResultXml.setMsgPath(filePath);
            return "迁移文件失败";
        }

        return null;
    }

    /**
     * 　* @Description: 运抵理货回执处理逻辑
     * 　* @Author: ps
     * 　* @Date: 2023/2/27 0027 17:46
     *
     */
    private String receive2(String messageType, String messageId, String receiverId, String content, String xmlType,
                          File file, String path1, String bwResultNoDeclarePath,String bwResultAnalPath,String bwResultBakPath,BaseResultXml baseResultXml){

        try {
            if (!(messageId.substring(4, 13).equals(receiverId.substring(4, 13)))) {
                //文件移动至无来源数据目录(回执报文未申报目录)
                PublicUtil.deleteFileOrDirectory(file.getPath());
                PublicUtil.createFile(bwResultNoDeclarePath, content);
                baseResultXml.setFlag("2");
                baseResultXml.setErrorMsg("回执报文无关联：messageId和ReceiverId不相等");
                baseResultXml.setMsgPath(bwResultNoDeclarePath);
                baseResultXmlMapper.updateById(baseResultXml);
                return null;
            }


            //判断 xmlType为1是单一窗口successed回执,xmlType为3单一窗口failed回执,xmlType为2海关回执
            if ("1".equals(xmlType) || "3".equals(xmlType)) {
                // 单一窗口回执
                if (airTypeList.contains(messageType)) {
                    // 空运
                    path1 = kToDzPath + path1;
                } else {
                    // 水运
                    path1 = sToDzPath + path1;
                }
            } else {
                // 海关回执
                if (airTypeList.contains(messageType)) {
                    // 空运
                    path1 = kToHaiguanPath + path1;
                } else {
                    // 水运
                    path1 = sToHaiguanPath + path1;
                }
            }

            //删除回执并创建备份回执
            PublicUtil.deleteFileOrDirectory(file.getPath());
            // 将回执报文迁移至对应目录
            PublicUtil.createFile(path1, content);
            // 回执待解析 没有重发过的回执才进待解析目录，防止重发过后的重复解析
            if (baseResultXml.getRepeatTime() == 0) {
                PublicUtil.createFile(bwResultAnalPath, content);
            }
            // 回执备份
            PublicUtil.createFile(bwResultBakPath, content);
        } catch (Exception e) {
            log.info("{}报文{}迁移失败：{}", messageType, new Date(), e.getMessage());
            // 文件迁移到备份目录的ERROR文件里
            String filePath = PublicUtil.moveFile(file, msgResultErrorDir, "ERROR");
            baseResultXml.setMsgPath(filePath);
            return "迁移文件失败";
        }

        return null;
    }
}
