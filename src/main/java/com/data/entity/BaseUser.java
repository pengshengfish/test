package com.data.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 通道用户表
 * </p>
 *
 * @author ps
 * @since 2022-09-08
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("MT_BASE_USER")
public class BaseUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId("ID")
    private String id;

    /**
     * 组织机构代码
     */
    @TableField("DEPT_NO")
    private String deptNo;

    /**
     * 业务类型（默认MFT）
     */
    @TableField("MESSAGE_TYPE")
    private String messageType;

    /**
     * 企业海关编码
     */
    @TableField("TRADE_CODE")
    private String tradeCode;

    /**
     * 企业名称
     */
    @TableField("TRADE_NAME")
    private String tradeName;

    /**
     * 平台标识 0 api接口 1: web系统 2：其它
     */
    @TableField("PLAT_ID")
    private String platId;

    /**
     * 通道用户名
     */
    @TableField("USER_ID")
    private String userId;

    /**
     * 国标版用户名
     */
    @TableField("GB_ID")
    private String gbId;

    /**
     * 合同到期日期
     */
    @TableField("CONTR_DATE")
    private LocalDateTime contrDate;

    /**
     * 用户类别 0:普通用户 1：平台用户 2：其它
     */
    @TableField("USER_TYPE")
    private String userType;

    /**
     * 联系人
     */
    @TableField("LINK_MAN")
    private String linkMan;

    /**
     * 联系方式
     */
    @TableField("TEL")
    private String tel;

    /**
     * 联系邮箱
     */
    @TableField("EMAIL")
    private String email;

    /**
     * 创建人
     */
    @TableField("CREATE_USER")
    private String createUser;

    /**
     * 创建时间
     */
    @TableField("CREATE_DATE")
    private LocalDateTime createDate;

    /**
     * 更新时间
     */
    @TableField("UPDATE_DATE")
    private LocalDateTime updateDate;

    /**
     * 更新人
     */
    @TableField("UPDATE_USER")
    private String updateUser;


}
