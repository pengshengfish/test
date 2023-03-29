package com.data.entity;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @Author: xuyang
 * @Date: 2022/9/9  11:25
 */
@XmlRootElement(name = "PlatImpMsg")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class PlatImpMsg {

    @XmlElement(name = "ImpHead")
    private ImpHead impHead;

    @XmlElement(name = "ImpBody")
    private ImpBody impBody;
}
