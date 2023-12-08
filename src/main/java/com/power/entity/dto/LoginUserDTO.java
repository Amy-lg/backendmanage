package com.power.entity.dto;

import lombok.Data;

/**
 * 用户登录传输参数实体类
 */
@Data
public class LoginUserDTO {
    /**
     * 用户名
     */
    private String username;
    /**
     * 密码
     */
    private String password;
    /**
     * 验证码
     */
    private String captchaCode;
}
