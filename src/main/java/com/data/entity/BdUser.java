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
 * 大数据贸易用户表
 * </p>
 *
 * @author ps
 * @since 2022-10-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("BD_USER")
public class BdUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId("ID")
    private String id;

    /**
     * 账号
     */
    @TableField("ACCOUNT")
    private String account;

    /**
     * 密码
     */
    @TableField("PASSWORD")
    private String password;

    /**
     * 企业名称
     */
    @TableField("COP_NAME")
    private String copName;

    /**
     * 统一社会信用代码
     */
    @TableField("COP_CODE_SCC")
    private String copCodeScc;

    /**
     * kafka服务器信息
     */
    @TableField("BROKER")
    private String broker;

    /**
     * 上行topic
     */
    @TableField("TC_SD")
    private String tcSd;

    /**
     * 下行topic
     */
    @TableField("TC_RX")
    private String tcRx;

    /**
     * 创建时间
     */
    @TableField("CREATE_TIME")
    private LocalDateTime createTime;

    /**
     * 创建人
     */
    @TableField("CREATE_USER")
    private String createUser;

    /**
     * 级别 0：江苏省电子口岸  1：企业  2：代理  3：口岸 4:舱单
     */
    @TableField("P_LEVEL")
    private String pLevel;


    /**
     * 联系人
     */
    @TableField("CONTACT_USER")
    private String contactUser;

    /**
     * 联系人电话
     */
    @TableField("CONTACT_PHONE")
    private String contactPhone;

    /**
     * 营业执照路径
     */
    @TableField("BUS_LICENSE_PATH")
    private String busLicensePath;

    /**
     * 客户端账号
     */
    @TableField("CLIENT_ACCOUNT")
    private String clientAccount;

    /**
     * 客户端密码
     */
    @TableField("CLIENT_PASSWORD")
    private String clientPassword;
}
