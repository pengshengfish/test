package com.data.util;


import lombok.Data;

import java.util.List;

/**
 * @Description: 新舱单回执接口返回Object
 * @Author: xuyang
 * @Date: 2022/9/13  17:20
 */
@Data
public class BwReceiptResultObject {

    private boolean success = true;

    private String error_msg;

    private List data;

    private String delete_id;


}
