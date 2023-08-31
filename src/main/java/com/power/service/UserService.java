package com.power.service;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.power.common.constant.ResultStatusCode;
import com.power.entity.User;
import com.power.entity.dto.UserDTO;
import com.power.exception.ServiceException;
import com.power.mapper.UserMapper;
import com.power.utils.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService extends ServiceImpl<UserMapper, User> {
    public boolean updateUser(User user) {
        /*if (user.getId() == null) {
            // mybatis-plus 提供的方法
            return save(user);
        } else {
            return updateById(user);
        }*/
        return saveOrUpdate(user);
    }

    public UserDTO userLogin(UserDTO userDTO) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", userDTO.getUsername());
        queryWrapper.eq("password", userDTO.getPassword());
        User loginUser;
        try {
            loginUser = getOne(queryWrapper);
        }catch (Exception e) {
            throw new ServiceException(ResultStatusCode.ERROR_1.getCode(), "系统错误");
        }
        // 如果登录用户不为空，直接登录
        if (loginUser != null) {
            BeanUtil.copyProperties(loginUser, userDTO, true);
            String token = TokenUtils.genToken(loginUser.getId().toString(), loginUser.getPassword());
            userDTO.setToken(token);
            return userDTO;
        } else {
            // 否则，将传进来的userDTO置为空对象返回，由controller层再次进行一个判断
            userDTO.setUsername(null);
            userDTO.setPassword(null);
            return userDTO;
        }
    }

//    @Autowired
//    private UserMapper userMapper;

    /*public int update(User user) {
        if (user.getId() == null) { // id为空时表示新增
            return userMapper.insert(user);
        } else {
            return userMapper.update(user); // id不为空表示修改
        }
    }*/


    // 删除用户
/*    public int deleteById(Integer id) {
        // 简单判断一下此id用户是否存在
        if (id != null) {
            int delete = userMapper.deleteById(id);
            return delete;
        }
        return 0;
    }*/
}
