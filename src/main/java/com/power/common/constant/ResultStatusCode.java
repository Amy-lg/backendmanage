package com.power.common.constant;

import lombok.Getter;

/**
 * 返回结果枚举类
 */
@Getter
public enum ResultStatusCode {

    /**
     * 正常返回
     */
    OK_0(0, "OK_0"),

    SUCCESS_UPLOAD(200, "数据上传成功"),

    SUCCESS_UPDATE(201, "备注信息保存成功"),
    SUCCESS_EMPTY(201, "备注信息内容为空"),

    /**
     * 错误
     */
    ERROR_1(1, "error"),

    ERROR_USER_001(001, "用户名或密码错误"),


    FILE_TYPE_ERROR(5004, "文件类型错误"),
    CONDITION_ERROR(5005, "查询条件错误，请重新输入"),


    EXCEPTION_USER_1001(1001, "用户信息不存在，请重新输入"),

    ERROR_IMPORT(5001, "数据信息导入失败")
    ;


    private final int code;

    private final String msg;


    ResultStatusCode(int statusCode, String reminderMsg) {
        this.code = statusCode;
        this.msg = reminderMsg;
    }
}
