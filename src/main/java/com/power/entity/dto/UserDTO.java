package com.power.entity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 接受前端用户登录请求的参数
 */
@Data
public class UserDTO implements Serializable {
    private String username;
    private String password;
    private String realName;
    private String phone;
    private String department;
    private String post;
    private String role;
    private String loginStatus;
    private String email;
    private String projectCounty;

    private String token;
}
