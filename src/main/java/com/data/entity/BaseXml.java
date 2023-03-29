package com.data.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * <p>
 * 舱单报文表
 * </p>
 *
 * @author ps
 * @since 2022-09-08
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("MT_BASE_XML")
public class BaseXml implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId("ID")
    private String id;

    /**
     * 报文编号
     */
    @TableField("MESSAGE_ID")
    private String messageId;

    /**
     * 报文类型
     */
    @TableField("MESSAGE_TYPE")
    private String messageType;

    /**
     * 报文备份全路径
     */
    @TableField("MSG_PATH")
    private String msgPath;

    /**
     * 申报时间
     */
    @TableField("CREATE_DATE")
    private LocalDateTime createDate;

    /**
     * 申报单位组织机构代码
     */
    @TableField("COP_CODE")
    private String copCode;

    /**
     * 国标版用户名
     */
    @TableField("GB_ID")
    private String gbId;

    /**
     * 报文功能代码
     */
    @TableField("FUNCTION_CODE")
    private String functionCode;

    /**
     * 报文发送时间
     */
    @TableField("SEND_TIME")
    private String sendTime;

    /**
     * 用户类别 0:普通用户 1：平台用户 2：其它
     */
    @TableField("USER_TYPE")
    private String userType;

    /**
     * 0:接收 1:已转换(已改造)
     */
    @TableField("FLAG")
    private String flag;

    /**
     * 所属平台代码
     */
    @TableField("ACCESS_CODE")
    private String accessCode;

    /**
     * 改造失败原因
     */
    @TableField("ERROR_MSG")
    private String errorMsg;

    /**
     * 重发次数
     */
    @TableField("REPEAT_TIME")
    private Integer repeatTime;

    /**
     * 重发时间
     */
    @TableField("REPEAT_DATE")
    private LocalDateTime repeatDate;

}
