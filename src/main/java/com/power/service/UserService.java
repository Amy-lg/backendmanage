package com.power.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.power.entity.User;
import com.power.mapper.UserMapper;
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
