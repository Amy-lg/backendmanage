package com.power.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.power.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

// 通过配置@MapperScan注解，可以代替@Mapper
@Mapper
public interface UserMapper extends BaseMapper<User> {

    // 查询所有用户
    @Select("select * from sys_user")
    List<User> findAll();

    // 修改用户信息
    int update(User user);

    // 新增用户信息
//    @Insert("insert into sys_user(username,password,nickname,email,phone,address) values(#{username}, #{password}, #{nickname}, #{email}, #{phone},#{address})")
//    int insert(User user);

    // 删除
    @Delete("delete from sys_user where id = #{id}")
    int deleteById(@Param("id") Integer id);

    //分页查询
    @Select("select * from sys_user where username like concat('%', #{username}, '%') limit #{pageNum}, #{pageSize}")
    List<User> findPage(Integer pageNum, Integer pageSize, String username);

    @Select("select count(*) from sys_user where username like concat('%', #{username}, '%')")
    Integer selectTotal(String username);
}
