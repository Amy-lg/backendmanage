package com.power.utils;

import com.power.common.Result;
import com.power.common.constant.ResultStatusCode;

/**
 * 返回类型包装类
 */
public class ResultUtils {

    /**
     * 成功时返回的值(有数据)
     * @param data 数据值
     * @return Result
     */
    public static Result success(Object data) {
        return new Result(ResultStatusCode.OK_0, data);
    }

    /**
     * 成功时返回的值(没有数据)
     * @return
     */
    public static Result success() {
        return new Result(ResultStatusCode.OK_0, "数据信息为空");
    }

    /**
     * 错误时返回
     * @param resultStatusCode
     * @param msg
     * @return
     */
    public static Result error(ResultStatusCode resultStatusCode, String msg) {
        return new Result(resultStatusCode, msg);
    }

    public static Result error(int code, String msg) {
        return new Result(code, msg);
    }
}
