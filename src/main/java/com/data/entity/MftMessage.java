package com.data.entity;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @Author: xuyang
 * @Date: 2022/9/6  13:49
 */
@XmlRootElement(name = "MFTMessage")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class MftMessage {

    @XmlElement(name = "MFTHead")
    private MftHead mftHead;

    @XmlElement(name = "MFTData")
    private MftData mftData;

}
