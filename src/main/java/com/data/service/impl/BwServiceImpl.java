package com.data.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.data.entity.*;
import com.data.mapper.BaseDeptMapper;
import com.data.mapper.BaseResultXmlMapper;
import com.data.mapper.BaseUserMapper;
import com.data.mapper.BaseXmlMapper;
import com.data.service.BwService;
import com.data.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import sun.misc.BASE64Encoder;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @Author: xuyang
 * @Date: 2022/9/7  14:13
 */
@Service("bwService")
@Slf4j
@Transactional(rollbackFor = {Exception.class})
public class BwServiceImpl implements BwService {

    @Value("${SM4Key}")
    private String sm4Key;

    @Value("${msgDir}")
    private String msgDir;

    @Autowired(required = false)
    private BaseUserMapper baseUserMapper;

    @Autowired(required = false)
    private BaseDeptMapper baseDeptMapper;

    @Autowired(required = false)
    private BaseResultXmlMapper baseResultXmlMapper;

    @Autowired(required = false)
    private BaseXmlMapper baseXmlMapper;

    /**
     * @Description: 上传报文
     * @Author: xuyang
     * @Date: 2022/9/7  14:16
     */
    @Override
    public BwResultObject uploadBw(Map<String, Object> params) {
        log.info("申报数据：{}", params.toString());
        BwResultObject result = new BwResultObject();
        //用户账号
        String userid = String.valueOf(params.get("userid"));
        //鉴权码
        String sign = String.valueOf(params.get("sign"));
        //报文字符串
        String data = String.valueOf(params.get("data"));
        String decryptData = null;
        //鉴权码校验
        Map flagMap = signCheck(sign);
        int flag = Integer.parseInt(String.valueOf(flagMap.get("checkFlag")));
        //获得 用户类别 0:普通用户 1：平台用户 2：其它
        String userType = String.valueOf(flagMap.get("userType"));
        if (flag != 1) {
            result.setSuccess(false);
            if (flag == -1) {
                result.setError_msg("解码失败");
            } else if (flag == -2) {
                result.setError_msg("已过期");
            } else if (flag == -3) {
                result.setError_msg("用户没有权限");
            } else if (flag == -4) {
                result.setError_msg("鉴权码错误");
            }
            return result;
        }
        //对data进行base解密后再解压缩
        try {
            decryptData = ZipUtils.uncompress(data);
        } catch (Exception e) {
            log.error("解码失败{}", e.getMessage());
            result.setSuccess(false);
            result.setError_msg("解密失败！");
            return result;
        }
        //获取除Declaration节点以外的报文实体类
        decryptData = decryptData.replaceAll(" xmlns=\"urn:Declaration:datamodel:standard:CN:MT2101:1\"", "")
                .replaceAll(" xmlns=\"urn:Declaration:datamodel:standard:CN:MT1101:1\"", "")
                .replaceAll(" xmlns=\"urn:Declaration:datamodel:standard:CN:MT4101:1\"", "")
                .replaceAll(" xmlns=\"urn:Declaration:datamodel:standard:CN:MT8104:1\"", "")
                .replaceAll(" xmlns=\"urn:Declaration:datamodel:standard:CN:MT8105:1\"", "")
                .replaceAll(" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"", "")
                .replaceAll("<\\?xml version=\"1.0\" encoding=\"UTF-8\"\\?>", "");
        MftMessage mftMessage = (MftMessage) XMLUtil.convertXmlStrToObject(MftMessage.class, decryptData);
        if (ObjectUtils.isEmpty(mftMessage)) {
            result.setSuccess(false);
            result.setError_msg("报文格式出错，请检查并重新上传！");
            return result;
        }

        String messageId = mftMessage.getMftData().getManifest().getManifestHead().getMessageId();
        String copCode = "";
        String messageType = mftMessage.getMftData().getManifest().getManifestHead().getMessageType();
        String functionCode = mftMessage.getMftData().getManifest().getManifestHead().getFunctionCode();
        String sendTime = mftMessage.getMftData().getManifest().getManifestHead().getSendTime();

        String senderId = mftMessage.getMftData().getManifest().getManifestHead().getSenderId();
        // 获取组织机构代码
        senderId = senderId.substring(4, 13);

        // 通道用户表中的标准版账号
        String gbId = (String) flagMap.get("gbId");
        //如果用户类别为平台用户,则根据CopCode值去通道企业表中查询是否存在这家企业
        List<BaseDept> baseDeptList = null;
        BaseXml baseXml = new BaseXml();
        if ("1".equals(userType)) {
            String msg = "";
            QueryWrapper<BaseDept> baseDeptQueryWrapper = new QueryWrapper<>();
            // 通关云独有逻辑
            if ("MA1W6AK72".equals(userid)) {
                baseDeptQueryWrapper.eq("DEPT_NO", senderId);
                msg = "企业" + senderId + "没有权限";
            } else {
                baseDeptQueryWrapper.eq("GB_ID", userid);
                msg = "国标版账号" + userid + "没有权限";
            }
            baseDeptList = baseDeptMapper.selectList(baseDeptQueryWrapper);

            if (baseDeptList == null || baseDeptList.size() <= 0) {
                result.setSuccess(false);
                result.setError_msg(msg);
                return result;
            }
            copCode = baseDeptList.get(0).getDeptNo();
            gbId = baseDeptList.get(0).getGbId();
            //向舱单报文表插入记录 为平台用户
            baseXml.setUserType("1");
            baseXml.setAccessCode(flagMap.get("accessCode").toString());
        } else {
            //普通用户无企业数据 0代表普通用户
            baseXml.setUserType("0");
        }
        //向舱单报文表 插入一条记录
        baseXml.setId(UUID.randomUUID().toString().replaceAll("-", ""));
        baseXml.setMessageId(messageId);
        baseXml.setFunctionCode(functionCode);
        baseXml.setMessageType(messageType);
        baseXml.setCopCode(copCode);
        baseXml.setMsgPath(null);
        baseXml.setGbId(gbId);
        baseXml.setCreateDate(LocalDateTime.now());
        baseXml.setSendTime(sendTime);
        //flag 默认为0 后面定时任务程序改造完报文之后 flag改为1 代表已转换
        baseXml.setFlag("0");
        baseXmlMapper.insert(baseXml);

        String fileName = msgDir + messageId + "$" + copCode + ".xml";
        //创建报文并写入然后存放到待改造目录下
        try {
            PublicUtil.createFile(fileName, decryptData);
        } catch (IOException e) {
            e.printStackTrace();
            //手动回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            result.setSuccess(false);
            result.setError_msg("创建报文失败！");
            return result;
        }
        result.setError_msg("接受成功");
        return result;
    }

    /**
     * @Description: 获取、删除回执报文
     * @Author: xuyang
     * @Date: 2022/9/13  17:31
     */
    @Override
    public BwReceiptResultObject listAndDelResultBw(Map<String, Object> params) {
        BwReceiptResultObject bwReceiptResultObject = new BwReceiptResultObject();
        //鉴权码
        String sign = String.valueOf(params.get("sign"));
        //组织机构代码
        String orgCode = String.valueOf(params.get("orgcode"));
        //操作类型
        String type = String.valueOf(params.get("type"));
        //删除主键
        String deleteId = String.valueOf(params.get("delete_id"));
        //鉴权码校验
        Map flagMap = signCheck(sign);
        int flag = Integer.parseInt(String.valueOf(flagMap.get("checkFlag")));
        if (flag != 1) {
            bwReceiptResultObject.setSuccess(false);
            if (flag == -1) {
                bwReceiptResultObject.setError_msg("解码失败");
            } else if (flag == -2) {
                bwReceiptResultObject.setError_msg("已过期");
            } else if (flag == -3) {
                bwReceiptResultObject.setError_msg("没有权限");
            } else if (flag == -4) {
                bwReceiptResultObject.setError_msg("鉴权码错误");
            }
            return bwReceiptResultObject;
        }
        QueryWrapper<BaseXml> baseXmlQueryWrapper = new QueryWrapper<>();
        baseXmlQueryWrapper.eq("COP_CODE", orgCode);
        List<BaseXml> baseXmlList = baseXmlMapper.selectList(baseXmlQueryWrapper);
        if (baseXmlList == null || baseXmlList.size() == 0) {
            bwReceiptResultObject.setSuccess(false);
            bwReceiptResultObject.setError_msg("当前企业还未申报");
            return bwReceiptResultObject;
        }

        String delete_id = null;
        List data = new ArrayList();
        //0代表查看回执
        if ("0".equals(type)) {
            delete_id = UUID.randomUUID().toString().replaceAll("-", "");
            //查询出当前企业最多20条最早的回执
            List<BaseResultXml> baseResultXmlList = baseResultXmlMapper.listResultBwByOrgCode(orgCode);
            if (baseResultXmlList != null && baseResultXmlList.size() > 0) {
                for (BaseResultXml baseResultXml : baseResultXmlList) {
                    try {
                        baseResultXml.setGroupId(delete_id);
                        if ("1".equals(baseResultXml.getXmlType())) {
                            //响应报文处理
                            String content = PublicUtil.readFile(new File(baseResultXml.getMsgPath()));
                            String manifestXml = PublicUtil.getXmlAttrValue(content, "PlatImpRet.RetBizData.Manifest");
                            //格式化美化xml
                            manifestXml = PublicUtil.beautifyXml(manifestXml);
                            //Base64加密，并存放在data数组中
                            BASE64Encoder base64Encoder = new BASE64Encoder();
                            manifestXml = base64Encoder.encode(manifestXml.getBytes("UTF-8"))
                                    .replaceAll("\r", "")
                                    .replaceAll("\n", "");
                            Map map = new HashMap();
                            map.put("list", manifestXml);
                            data.add(map);
                        } else {
                            //业务报文处理
                            String content = PublicUtil.readFile(new File(baseResultXml.getMsgPath()));
                            //Base64加密，并存放在data数组中
                            BASE64Encoder base64Encoder = new BASE64Encoder();
                            content = base64Encoder.encode(content.getBytes("UTF-8"))
                                    .replaceAll("\r", "")
                                    .replaceAll("\n", "");
                            Map map = new HashMap();
                            map.put("list", content);
                            data.add(map);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        bwReceiptResultObject.setSuccess(false);
                        bwReceiptResultObject.setError_msg("报文处理失败");
                        return bwReceiptResultObject;
                    }
                    //更新delete_id
                    baseResultXmlMapper.updateById(baseResultXml);
                    bwReceiptResultObject.setDelete_id(baseResultXml.getGroupId());
                    bwReceiptResultObject.setData(data);
                    bwReceiptResultObject.setError_msg("接受成功");
                }
            } else {
                bwReceiptResultObject.setSuccess(true);
                bwReceiptResultObject.setError_msg("当前企业无回执");
                return bwReceiptResultObject;
            }
        } else {
            //1代表删除回执
            QueryWrapper<BaseResultXml> baseResultXmlQueryWrapper = new QueryWrapper<>();
            baseResultXmlQueryWrapper.eq("GROUP_ID", deleteId);
            BaseResultXml baseResultXml = new BaseResultXml();
            baseResultXml.setDeleteFlag("1");
            //更新删除标志为1
            baseResultXmlMapper.update(baseResultXml, baseResultXmlQueryWrapper);
            bwReceiptResultObject.setError_msg("删除成功");
        }
        return bwReceiptResultObject;
    }


    /**
     * @Description: 鉴权码校验
     * @Author: xuyang
     * @Date: 2022/9/13  17:52
     */
    public Map signCheck(String sign) {
        HashMap<String, Object> map = new HashMap<>();
        //flag：1为校验成功 -1为解码失败 -2为已过期 -3为没有权限 -4鉴权码错误
        int flag = 0;
        String decryptSign = null;
        //对sign进行SM4国产解密
        try {
            decryptSign = Sm4Util.decryptEcb(sm4Key, sign);
        } catch (Exception e) {
            log.error("SM4国产解码失败{}", e.getMessage());
            map.put("checkFlag", -1);
            return map;
        }
        if ("error".equals(decryptSign)) {
            log.error("SM4国产解码失败");
            map.put("checkFlag", -1);
            return map;
        }
        String[] signNew = decryptSign.split("\\$");
        if (signNew.length != 2) {
            map.put("checkFlag", -4);
            return map;
        }
        //获取通道用户名
        String userId = signNew[0];
        //获取组织机构代码
        String deptNo = signNew[1];
        QueryWrapper<BaseUser> baseUserQueryWrapper = new QueryWrapper<>();
        baseUserQueryWrapper.eq("USER_ID", userId).eq("DEPT_NO", deptNo);
        List<BaseUser> baseUserList = baseUserMapper.selectList(baseUserQueryWrapper);
        if (baseUserList != null && baseUserList.size() > 0) {
            BaseUser baseUser = baseUserList.get(0);
            //获得 用户类别 0:普通用户 1：平台用户 2：其它
            map.put("userType", baseUser.getUserType());
            map.put("gbId", baseUser.getGbId());
            map.put("accessCode", deptNo);
            //合同到期日期
            LocalDateTime contrDate = baseUser.getContrDate();
            //如果合同到期日期大于当前时间 则return
            if (LocalDateTime.now().isAfter(contrDate)) {
                map.put("checkFlag", -2);
                return map;
            }
        } else {
            map.put("checkFlag", -3);
            return map;
        }
        map.put("checkFlag", 1);
        return map;
    }

    public static void main(String[] args) {
        String senderId = "2327755869230_NJ00010002";
        System.out.println(senderId.substring(4, 13));
    }

}
