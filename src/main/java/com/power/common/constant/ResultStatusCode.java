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

    /**
     * 错误
     */
    ERROR_1(1, "error"),

    ERROR_USER_001(001, "用户名或密码错误"),





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