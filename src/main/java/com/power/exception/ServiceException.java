package com.power.exception;

import com.power.common.constant.ResultStatusCode;
import lombok.Getter;

/**
 * 服务类异常
 */
@Getter
public class ServiceException extends RuntimeException{

    private int code;

    public ServiceException(int code, String msg) {
        super(msg);
        this.code = code;
    }

}
