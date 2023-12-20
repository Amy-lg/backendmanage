package com.power.service;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.power.common.constant.ResultStatusCode;
import com.power.entity.User;
import com.power.entity.dto.LoginUserDTO;
import com.power.entity.dto.UserDTO;
import com.power.entity.query.UserQuery;
import com.power.exception.ServiceException;
import com.power.mapper.UserMapper;
import com.power.utils.Md5Util;
import com.power.utils.TokenUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

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

    public UserDTO userLogin(LoginUserDTO loginUserDTO, HttpServletRequest request,
                             Map<String, Object> verifyMap) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        // 获取后端存储的验证码和过期时间
        String captVerifyCode = (String) verifyMap.get("captVerifyCode");
        long expirationTime = (long) verifyMap.get("expTime");
        if (!(System.currentTimeMillis() > expirationTime)) {
            // 前端传入需要验证的验证码
            String captchaCode = loginUserDTO.getCaptchaCode();
            // 先判断验证码是否正确
            if (captVerifyCode != null && captVerifyCode.equals(captchaCode)) {
                // 将验证码清除
                if (verifyMap.containsKey(captVerifyCode)) {
                    verifyMap.remove("captVerifyCode");
                }
                if (verifyMap.containsKey("expTime")) {
                    verifyMap.remove("expTime");
                }
                request.getSession().removeAttribute("captVerifyCode");

                queryWrapper.eq("username", loginUserDTO.getUsername());
                queryWrapper.eq("password", loginUserDTO.getPassword());
                User loginUser;
                UserDTO userDTO = new UserDTO();
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
            } else {
                return null;
            }
        }
        return null;
    }


    /**
     * 查询用户信息
     * @param userQuery
     * @return
     */
    public IPage<User> findOrSearch(UserQuery userQuery) {

        Integer pageNum = userQuery.getPageNum();
        Integer pageSize = userQuery.getPageSize();
        String username = userQuery.getUsername();

        IPage<User> userPage = new Page<>(pageNum, pageSize);
        if (username != null && !"".equals(username)) {
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("username", username);
            IPage<User> searchPage = this.page(userPage, queryWrapper);
            return searchPage;
        }
        // 返回全部用户信息
        return page(userPage);
    }


    /**
     * admin添加新用户
     * @param user
     * @return
     */
    public String addNewUser(User user) {

        // 只有admin才能添加用户，获取当前登录的用户信息
        User currentUser = TokenUtils.getCurrentUser();
        String role = currentUser.getRole();
        if (!role.isBlank() && role.contains("超级管理员")) {
            // MD5加密
            String md5Pwd = Md5Util.encrypt(user.getPassword());
            System.out.println("Md5Pwd = " + md5Pwd);
            user.setPassword(md5Pwd);
            // 登录用户为管理员，可以添加
            this.saveOrUpdate(user);
            return ResultStatusCode.SUCCESS_ADD_LOGIN_USER.getMsg();
        }
        return ResultStatusCode.ERROR_ADD_LOGIN_USER.getMsg();
    }


    /**
     * 管理员批量删除
     * @param ids
     * @return
     */
    public String deleteBatch(List<Integer> ids) {

        // 获取当前登录者是否为管理员
        User currentUser = TokenUtils.getCurrentUser();
        String currentUserRole = currentUser.getRole();
        if (!currentUserRole.isBlank() && currentUserRole.contains("超级管理员")) {
            // 用户为管理员，进行操作
            if (ids != null && ids.size() >= 1) {
                boolean delSta = this.removeBatchByIds(ids);
                if (delSta) {
                    return ResultStatusCode.SUCCESS_DELETE_USER.getMsg();
                }
            }
            return ResultStatusCode.ERROR_DEL_USER_1002.getMsg();
        }
        return ResultStatusCode.ERROR_DEL_LOGIN_USER.getMsg();
    }


    /**
     * 验证前端传入的验证码是否正确
     * @param captchaCode
     * @param request
     * @return
     */
    private boolean getCheckCaptcha(String captchaCode, HttpServletRequest request) {

        try {
            String verifyCode = (String) request.getSession().getAttribute("captVerifyCode");
            // 获取会话域中的验证码信息，并转为小写
            String sessionCaptchaCode = String.valueOf(request.getSession().getAttribute("captVerifyCode")).toLowerCase();
            // 将前端传过来的captchaCode转为小写，
            String checkCaptchaCode = captchaCode.toLowerCase();
            // 验证
            return !"".equals(sessionCaptchaCode) && !"".equals(checkCaptchaCode)
                    && sessionCaptchaCode.equals(checkCaptchaCode);
        }catch (Exception e) {
            return false;
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
