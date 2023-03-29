package com.data.entity;


import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * @Author: xuyang
 * @Date: 2022/9/7  9:06
 */
@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class ManifestHead {

    /**
     * 报文编号
     */
    @XmlElement(name = "MessageID")
    private String messageId;

    /**
     * 报文功能代码
     */
    @XmlElement(name = "FunctionCode")
    private String functionCode;

    /**
     * 报文类型代码
     */
    @XmlElement(name = "MessageType")
    private String messageType;

    /**
     * 发送方代码
     */
    @XmlElement(name = "SenderID")
    private String senderId;
    /**
     * 接受方代码
     */
    @XmlElement(name = "ReceiverID")
    private String receiverId;

    /**
     * 发送时间
     */
    @XmlElement(name = "SendTime")
    private String sendTime;
    /**
     * 报文版本号
     */
    @XmlElement(name = "Version")
    private String version;

}
