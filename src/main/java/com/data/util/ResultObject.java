package com.data.util;

public class ResultObject {

    private boolean success = true;

    private String message;

    private Object data;

    private String errorfield;

    private String msginfo;

    private ResultObject() {
    }

    public static ResultObject createInstance() {
        return createInstance(true);
    }

    public static ResultObject createInstance(boolean success) {
        return createInstance(success, (String) null);
    }

    public static ResultObject createInstance(boolean success, String message) {
        return createInstance(success, message, (Object) null);
    }

    public static ResultObject createInstance(boolean success, String message, Object data) {
        return createInstance(success, message, data, (String) null);
    }

    public static ResultObject createInstance(boolean success, String message, Object data, String errorfield) {
        ResultObject ro = new ResultObject();
        ro.setData(data);
        ro.setSuccess(success);
        if (message != null && !"".equals(message.trim())) {
            ro.setMessage(message);
        } else if (success) {
            ro.setMessage("成功");
        } else {
            ro.setMessage("失败");
        }

        ro.setErrorfield(errorfield);
        return ro;
    }

    public boolean isSuccess() {
        return this.success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return this.data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getErrorfield() {
        return this.errorfield;
    }

    public void setErrorfield(String errorfield) {
        this.errorfield = errorfield;
    }

    public String getMsginfo() {
        return this.msginfo;
    }

    public void setMsginfo(String msginfo) {
        this.msginfo = msginfo;
    }
}
