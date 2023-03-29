package com.data.util;

import lombok.Data;


/**
 * @Description: 新舱单报文接口返回Object
 * @Author: xuyang
 * @Date: 2022/9/14  15:27
 */
@Data
public class BwResultObject {

    private boolean success = true;

    private String error_msg;
}
