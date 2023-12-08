package com.power.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@TableName("sys_user")
public class User {
    @TableId(value = "id",type = IdType.AUTO)
    private Integer id;
    private String username;
    @JsonIgnore(value = false)
    private String password;
    /**
     * 真实姓名
     */
    private String realName;
    /**
     * 联系电话
     */
    private String phone;
    /**
     * 所属部门
     */
    private String department;
    /**
     * 岗位
     */
    private String post;
    /**
     * 角色；管理员 or 普通用户
     */
    private String role;
    /**
     * 状态
     */
    private String loginStatus;
}
