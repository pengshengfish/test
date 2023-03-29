package com.data.service;

import com.data.util.BwReceiptResultObject;
import com.data.util.BwResultObject;

import java.util.Map;

/**
 * @Author: xuyang
 * @Date: 2022/9/7  14:13
 */
public interface BwService {


    /**
     * @Description: 上传报文
     * @Author: xuyang
     * @Date: 2022/9/7  14:15
     */
    BwResultObject uploadBw(Map<String, Object> params);

    /**
     * @Description: 获取、删除回执报文
     * @Author: xuyang
     * @Date: 2022/9/13  17:31
     */
    BwReceiptResultObject listAndDelResultBw(Map<String, Object> params);
}
