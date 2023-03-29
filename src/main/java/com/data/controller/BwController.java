package com.data.controller;


import com.data.service.BwService;
import com.data.util.BwReceiptResultObject;
import com.data.util.BwResultObject;
import com.data.util.ZipUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @Author: xuyang
 * @Date: 2022/9/7  13:58
 */
@RestController
@Slf4j
@RequestMapping("/bwController")
public class BwController {

    @Autowired
    private BwService bwService;

    /**
     * @Description: 上传报文接口
     * @Param: {"userid":"用户ID","sign":"鉴权码","data":"报文base64编码后，压缩后的字符串"}
     * @Author: xuyang
     * @Date: 2022/9/7  14:03
     */
    @PostMapping(value = "/uploadBw")
    public BwResultObject uploadBw(@RequestBody Map<String, Object> params) {
        return bwService.uploadBw(params);
    }


    /**
     * @Description: 获取、删除回执接口
     * @Param: {"sign":"鉴权码","orgcode":"申报单位企业组织机构代码","type":"操作类型","delete_id":"删除主键"}
     * @Author: xuyang
     * @Date: 2022/9/13  17:29
     */
    @PostMapping(value = "/listAndDelResultBw")
    public BwReceiptResultObject deleteResultBw(@RequestBody Map<String, Object> params) {
        return bwService.listAndDelResultBw(params);
    }

    /**
     * @Description: 上传报文接口
     * @Param: {"userid":"用户ID","sign":"鉴权码","data":"报文base64编码后，压缩后的字符串"}
     * @Author: xuyang
     * @Date: 2022/9/7  14:03
     */
    @PostMapping(value = "/base64Compress")
    public String base64Compress(@RequestBody String params) {
        return ZipUtils.compress(params);
    }


}
