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
 * 舱单回执报文表
 * </p>
 *
 * @author ps
 * @since 2022-09-13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("MT_BASE_RESULT_XML")
public class BaseResultXml implements Serializable {

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
     * 报文功能代码
     */
    @TableField("FUNCTION_CODE")
    private String functionCode;

    /**
     * 回执报文发送时间
     */
    @TableField("SEND_TIME")
    private String sendTime;

    /**
     * 入库时间
     */
    @TableField("CREATE_DATE")
    private LocalDateTime createDate;

    /**
     * 删除标识 (默认：0，删除：1)
     */
    @TableField("DELETE_FLAG")
    private String deleteFlag;

    /**
     * 报文类型 1：响应回执(格式1) 2：业务回执(格式2)
     */
    @TableField("XML_TYPE")
    private String xmlType;

    /**
     * 申报单位组织机构代码
     */
    @TableField("COP_CODE")
    private String copCode;

    /**
     * 删除主键delete_id
     */
    @TableField("GROUP_ID")
    private String groupId;

    /**
     * 删除主键delete_id
     */
    @TableField("FAIL_INFO")
    private String failInfo;

    /**
     * 状态
     */
    @TableField("FLAG")
    private String flag;

    /**
     * 重发次数
     */
    @TableField("REPEAT_TIME")
    private Integer repeatTime;

    /**
     * 重发时间
     */
    @TableField("REPEAT_DATE")
    private Date repeatDate;

    /**
     * 失败原因
     */
    @TableField("ERROR_MSG")
    private String errorMsg;
}
