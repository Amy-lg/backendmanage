package com.power.vo.user;

import lombok.Data;

/**
 * 用户第一次登录，需要修改密码返回前端的视图类
 * @author cyk
 * @since 2023/12
 */
@Data
public class UpdUserInfoVO {
    private String username;
    private String oldPassword;
    private String newPassword;
}
