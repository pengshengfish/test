package com.data.entity;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;


/**
 * @Author: xuyang
 * @Date: 2022/9/6  13:53
 */
@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class MftHead {


    /**
     * 平台ID
     */
    @XmlElement(name = "PlatId")
    private String platId;

    /**
     * 签名信息
     */
    @XmlElement(name = "SignInfo")
    private String signInfo;

    /**
     * 申报单位组织机构代码
     */
    @XmlElement(name = "CopCode")
    private String copCode;

    /**
     * 自定义栏位
     */
    @XmlElement(name = "Note1")
    private String note1;

    /**
     * 自定义栏位
     */
    @XmlElement(name = "Note2")
    private String note2;


}
