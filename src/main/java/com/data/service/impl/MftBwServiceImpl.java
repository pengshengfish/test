package com.data.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.data.entity.BaseXml;
import com.data.entity.ImpBody;
import com.data.entity.ImpHead;
import com.data.entity.PlatImpMsg;
import com.data.mapper.BaseXmlMapper;
import com.data.service.MftBwService;
import com.data.util.PublicUtil;
import com.data.util.XMLUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.map.ListOrderedMap;
import org.dom4j.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import sun.misc.BASE64Encoder;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * @Description: 舱单报文
 * @Author: xuyang
 * @Date: 2022/9/8  15:16
 */
@Service("mftBwService")
@Slf4j
@Transactional(rollbackFor = {Exception.class})
public class MftBwServiceImpl implements MftBwService {

    @Value("${msgBakPath}")
    private String msgBakPath;

    @Value("${msgSendPath}")
    private String msgSendPath;

    @Value("${msgSendBakPath}")
    private String msgSendBakPath;

    @Value("${msgErrorDir}")
    private String msgErrorDir;

    @Autowired(required = false)
    private BaseXmlMapper baseXmlMapper;

    /**
     * @Description: 改造报文
     * @Author: xuyang
     * @Date: 2022/9/8  15:19
     */
    @Override
    @Async("taskExecutor")
    public void transform(List<File> fileList, CountDownLatch latch) {
        // 舱单报文开始改造
        for (File file : fileList) {
            // 读取xml文件内容
            String xmlStr = PublicUtil.readFile(file);
            Map<String, Object> map = new ListOrderedMap();
            try {
                map = PublicUtil.xmlToMap(xmlStr);
            } catch (DocumentException e) {
                e.printStackTrace();
            }
            this.transformBw(map, file);
        }
        latch.countDown();
    }


    public void transformBw(Map<String, Object> map, File file) {
        //获取Head节点下的MessageID、FunctionCode、MessageType、SendTime的值
        String messageId = null;
        String functionCode = null;
        String messageType = null;
        String sendTime = null;
        String copCode = null;
        //报文预备份全路径
        String bwBakPath = msgBakPath;
        //报文待发送全路径
        String bwSendPath = msgSendPath;
        //报文待发送备份路径
        String bwSendBakPath = msgSendBakPath;

        String gbId = null;
        try {
            Map mftMessageMap = (Map) map.get("MFTMessage");
            Map mftHeadMap = (Map) mftMessageMap.get("MFTHead");
            Map mftDataMap = (Map) mftMessageMap.get("MFTData");
            Map manifestMap = (Map) mftDataMap.get("Manifest");
            Map headMap = (Map) manifestMap.get("Head");
            messageId = String.valueOf(headMap.get("MessageID"));
            functionCode = String.valueOf(headMap.get("FunctionCode"));
            messageType = String.valueOf(headMap.get("MessageType"));
            sendTime = String.valueOf(headMap.get("SendTime"));
            copCode = String.valueOf(mftHeadMap.get("CopCode"));
        } catch (Exception e) {
            log.error("节点转换失败：{}", e.getMessage());
            //文件迁移到改造报文时出错目录的ERROR文件里
            PublicUtil.moveFile(file, msgErrorDir, "ERROR");
            return;
        }


        String fileName = file.getName();
        BaseXml baseXml = new BaseXml();
        try {
            //判断是否为重发报文
            if (fileName.contains("Repeat")) {
                String id = fileName.split("_").length>1?fileName.split("_")[1]:null;
                baseXml = baseXmlMapper.selectById(id);
            } else {
                // 通过messageId查询出企业国标版账号
                QueryWrapper<BaseXml> baseXmlQueryWrapper = new QueryWrapper<>();
                baseXmlQueryWrapper.eq("MESSAGE_ID", messageId).orderByDesc("CREATE_DATE");
                List<BaseXml> baseXmlList = baseXmlMapper.selectList(baseXmlQueryWrapper);
                baseXml = baseXmlList.get(0);
            }
            gbId = baseXml.getGbId();
            //备份报文名称：企业国标版账号$MessageType_FunctionCode$SSendTime$0$组织机构代码.xml
            bwBakPath += messageType + File.separator + gbId + "$" +
                    messageType + "_" + functionCode + "$S" + sendTime + "$0$" + copCode + ".xml";
            bwSendPath += gbId + "$" +
                    messageType + "_" + functionCode + "$S" + sendTime + "$0$" + copCode + ".xml";
            bwSendBakPath += LocalDateTime.now().format(DateTimeFormatter
                    .ofPattern("yyyyMMdd")) + File.separator + messageType + File.separator + gbId + "$" +
                    messageType + "_" + functionCode + "$S" + sendTime + "$0$" + copCode + ".xml";

            //更新baseXml 将报文预备份全路径更新上去 将baseXml里面flag更新为已转换(1)
            baseXml.setMsgPath(bwSendBakPath);
            baseXml.setFlag("1");
            baseXmlMapper.updateById(baseXml);
        } catch (Exception e) {
            log.error("数据库操作失败：{}", e.getMessage());
            //文件迁移到改造报文时出错目录的ERROR文件里
            PublicUtil.moveFile(file, msgErrorDir, "ERROR");
            return;
        }

        //报文改造
        String manifestXml = null;
        try {
            manifestXml = PublicUtil.getXmlAttrValue(PublicUtil.readFile(file), "MFTMessage.MFTData.Manifest");
            //根据MessageType替换<Manifest>
            manifestXml = manifestXml.replace("<Manifest>",
                    "<Manifest xmlns=\"urn:Declaration:datamodel:standard:CN:" + messageType + ":1\">");
            //格式化xml
//            manifestXml = PublicUtil.beautifyXml(manifestXml);
        }catch (Exception e) {
            log.error("获取报文业务节点出错：{}", e.getMessage());
            //文件迁移到改造报文时出错目录的ERROR文件里
            String filePath = PublicUtil.moveFile(file, msgErrorDir, "ERROR");
            baseXml.setMsgPath(filePath);
            //状态改为改造失败
            baseXml.setErrorMsg("获取报文业务节点出错");
            baseXml.setFlag("2");
            baseXmlMapper.updateById(baseXml);
            return;
        }

        String sentXml = null;
        try {
            //1.将格式化后的xml生成最终xml待发送版本， 2.迁移原始报文到备份目录中 3.将改造完的报文放入待发送目录下
            String bizData = null;
            //将格式化后的xml进行base64加密
            BASE64Encoder base64Encoder = new BASE64Encoder();
            bizData = base64Encoder.encode(manifestXml.getBytes("UTF-8"))
                    .replaceAll("\r", "")
                    .replaceAll("\n", "");
            PlatImpMsg platImpMsg = new PlatImpMsg();
            ImpHead impHead = new ImpHead();
            impHead.setUserName(gbId);
            impHead.setPlatId("SW00000020170802");
            ImpBody impBody = new ImpBody();
            impBody.setBizData(bizData);
            platImpMsg.setImpHead(impHead);
            platImpMsg.setImpBody(impBody);
            sentXml = XMLUtil.convertToXml(platImpMsg);
            sentXml = sentXml.replaceFirst("<PlatImpMsg>",
                    "<PlatImpMsg xsi:schemaLocation=\"http://www.chinaport.gov.cn/stp STP.xsd\" xmlns:stp=\"http://www.chinaport.gov.cn/stp\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">");
        } catch (Exception e) {
            log.error("报文转换为待发送报文格式时出错：{}", e.getMessage());
            //文件迁移到改造报文时出错目录的ERROR文件里
            String filePath = PublicUtil.moveFile(file, msgErrorDir, "ERROR");
            baseXml.setMsgPath(filePath);
            //状态改为改造失败
            baseXml.setErrorMsg("报文转换为待发送报文格式时出错");
            baseXml.setFlag("2");
            baseXmlMapper.updateById(baseXml);
            return;
        }

        try {
            //将待改造的报文迁移到待解析目录中
//            FileUtils.moveFile(file,new File(bwBakPath));
            String content = PublicUtil.readFile(file);
            PublicUtil.deleteFileOrDirectory(file.getPath());
            //没有重发过的报文才进待解析目录，防止重发过后的重复解析
            if (baseXml.getRepeatTime() == 0) {
                PublicUtil.createFile(bwBakPath, content);
            }
            //将改造完的报文放入待发送目录下和待发送备份目录
            PublicUtil.createFile(bwSendPath, sentXml);
            PublicUtil.createFile(bwSendBakPath, sentXml);
        } catch (Exception e) {
            log.error("报文迁移出错：{}", e.getMessage());
            //文件迁移到改造报文时出错目录的ERROR文件里
            String filePath = PublicUtil.moveFile(file, msgErrorDir, "ERROR");
            baseXml.setMsgPath(filePath);
            //状态改为改造失败
            baseXml.setErrorMsg("报文迁移出错");
            baseXml.setFlag("2");
            baseXmlMapper.updateById(baseXml);
            return;
        }
    }
}
