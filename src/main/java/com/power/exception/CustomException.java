package com.power.exception;

import com.power.common.Result;
import com.power.utils.ResultUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 自定义异常类
 */
public class CustomException {

    @ExceptionHandler(ServiceException.class)
    @ResponseBody
    public Result handle(ServiceException serviceException) {
        return ResultUtils.error(serviceException.getCode(), serviceException.getMessage());
    }
}
