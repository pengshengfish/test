package com.data.entity;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * @Author: xuyang
 * @Date: 2022/9/9  11:26
 */
@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class ImpHead {

    @XmlElement(name = "UserName")
    private String userName;

    @XmlElement(name = "PlatID")
    private String platId;
}
