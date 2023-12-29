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
    SUCCESS_INSERT(203, "新增数据信息成功"),
    SUCCESS_UPDATE_INFO(204, "更新数据信息成功"),
    SUCCESS_ADD_LOGIN_USER(205, "用户信息更新成功"),
    SUCCESS_DELETE_USER(206, "删除用户成功"),
    SUCCESS_MODIFY_PWD(207, "密码修改成功"),

    /**
     * 错误
     */
    ERROR_1(1, "error"),

    ERROR_USER_001(001, "用户名或密码错误"),
    ERROR_USER_002(002, "验证码输入错误或已过期，请刷新后重新输入"),
    ERROR_USER_003(003, "旧密码输入错误，请重新输入"),


    FILE_TYPE_ERROR(5004, "文件类型错误"),
    CONDITION_ERROR(5005, "查询条件错误，请重新输入"),


    EXCEPTION_USER_1001(1001, "账号或密码错误"),
    ERROR_DEL_USER_1002(1002, "删除失败，请选择要删除的用户编号"),

    ERROR_IMPORT(5001, "数据信息导入失败"),
    ERROR_IMPORT_001(5003, "数据信息导入失败，导入的Excel与数据库表不对应。请重新选择！"),
    ERROR_UPDATE(5002, "新增数据信息失败，工单编号不能为空"),
    ERROR_UPDATE_INFO(5009, "数据信息更新失败"),

    ERROR_ADD_LOGIN_USER(5007, "新增用户失败，请联系管理员"),
    ERROR_DEL_LOGIN_USER(5008, "删除用户失败，请联系管理员")
    ;


    private final int code;

    private final String msg;


    ResultStatusCode(int statusCode, String reminderMsg) {
        this.code = statusCode;
        this.msg = reminderMsg;
    }
}
