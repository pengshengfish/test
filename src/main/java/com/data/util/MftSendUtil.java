package com.data.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.map.ListOrderedMap;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


/**
 * @Author:chenxueqing
 * @Description: 报文推送kafka公共方法
 * @Time:2022/10/18 14:55
 */
@Slf4j
public class MftSendUtil {

    /**
     * @Author:chenxueqing
     * @Description: 参数1:报文类型 参数2:推送kafka数据(加密后报文)
     * @Time:2022/10/18 14:58
     */
    public static String mftSend(String type, String encryptData) {
        ListOrderedMap map = new ListOrderedMap();
        ListOrderedMap messageMap = new ListOrderedMap();
        ListOrderedMap headMap = new ListOrderedMap();
        map.put("Message", messageMap);
        messageMap.put("Head", headMap);
        //data为加密后的报文
        messageMap.put("Data", encryptData);
        //报文数据类型
        headMap.put("Type", type);
        // 获取当前时间，并且将他转化为指定格式
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        headMap.put("SendTime", currentTime);
        StringBuffer s = new StringBuffer();
        try {
            String mapToXml = PublicUtil.parseMap(map, s);
            String beautifyXml = PublicUtil.beautifyXml(mapToXml);
            return beautifyXml;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("map转xml出错：{}", e.getMessage());
            return null;
        }
    }
}
