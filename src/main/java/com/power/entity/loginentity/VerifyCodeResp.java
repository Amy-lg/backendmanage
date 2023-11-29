package com.power.entity.loginentity;

import lombok.Data;

import java.io.Serializable;

@Data
public class VerifyCodeResp implements Serializable {

    /**
     * 头参数
     */
    private String captchaKey;
    /**
     * 验证码图片
     */
    private String captchaImg;
}
