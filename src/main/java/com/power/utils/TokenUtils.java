package com.power.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.power.entity.User;
import com.power.service.UserService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Date;

/**
 * 集成JWT
 * @since 2023/8
 * @author cyk
 */
@Component
public class TokenUtils {

    private static UserService staticUserService;

    @Resource
    private UserService userService;

    @PostConstruct
    public void setStaticUserService() {
        staticUserService = userService;
    }

    /**
     * 生成Token
     * @param userId 用户id
     * @param sign 用户密码
     * @return token
     */
    public static String genToken(String userId, String sign) {
        return JWT.create().withAudience(userId) // 将userid保存到token中作为载荷
                .withExpiresAt(DateUtil.offsetHour(new Date(), 2)) // 设置token2小时过期
                .sign(Algorithm.HMAC256(sign)); // 以用户登录的password作为token的密钥
    }


    // 获取当前登录用户信息
    public static User getCurrentUser() {
        try {
            // 获取request请求
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            String token = request.getHeader("token");
            if (StrUtil.isNotBlank(token)) {
                String userId = JWT.decode(token).getAudience().get(0);
                return staticUserService.getById(Integer.valueOf(userId));
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
