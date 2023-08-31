package com.power.common;

import com.power.common.constant.ResultStatusCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result {
    private int code;
    private String msg;
    private Object data;

    public Result(ResultStatusCode resultStatusCode, Object data) {
        this.code = resultStatusCode.getCode();
        this.msg = resultStatusCode.getMsg();
        this.data = data;
    }

    public Result(ResultStatusCode resultStatusCode, String msg) {
        this.code = resultStatusCode.getCode();
        this.msg = msg;
        this.data = null;
    }

    public Result(int statusCode, String retMsg) {
        this.code = statusCode;
        this.msg = retMsg;
    }

}
