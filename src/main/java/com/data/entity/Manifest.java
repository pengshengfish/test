package com.data.entity;


import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;


/**
 * @Author: xuyang
 * @Date: 2022/9/6  14:42
 */
@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class Manifest {

    @XmlElement(name = "Head")
    private ManifestHead manifestHead;
}
