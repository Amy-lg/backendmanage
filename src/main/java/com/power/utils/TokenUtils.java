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

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

import java.util.Date;

/**
 * 集成JWT
 * @since 2023/8
 * @author cyk
 */
@Component
public class TokenUtils {

    private static UserService staticUserService;

    private static final String ALGORITHMS = "AES/ECB/PKCS5Padding";

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


    /**
     * 加密
     * @param content 内容
     * @param key     key
     * @return java.lang.String
     */
    public static String encrypt(String content, String key) {
        try {
            //获得密码的字节数组
            byte[] raw = key.getBytes();
            //根据密码生成AES密钥
            SecretKeySpec skey = new SecretKeySpec(raw, "AES");
            //根据指定算法ALGORITHM自成密码器
            Cipher cipher = Cipher.getInstance(ALGORITHMS);
            //初始化密码器，第一个参数为加密(ENCRYPT_MODE)或者解密(DECRYPT_MODE)操作，第二个参数为生成的AES密钥
            cipher.init(Cipher.ENCRYPT_MODE, skey);
            //获取加密内容的字节数组(设置为utf-8)不然内容中如果有中文和英文混合中文就会解密为乱码
            byte[] byteContent = content.getBytes(StandardCharsets.UTF_8);
            //密码器加密数据
            byte[] encodeContent = cipher.doFinal(byteContent);
            //将加密后的数据转换为字符串返回
            return Base64.encodeBase64String(encodeContent);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 解密
     * @param encryptStr 加密后的内容
     * @param decryptKey 解密key
     * @return java.lang.String
     */
    public static String decrypt(String encryptStr, String decryptKey) {
        try {
            //获得密码的字节数组
            byte[] raw = decryptKey.getBytes();
            //根据密码生成AES密钥
            SecretKeySpec skey = new SecretKeySpec(raw, "AES");
            //根据指定算法ALGORITHM自成密码器
            Cipher cipher = Cipher.getInstance(ALGORITHMS);
            //初始化密码器，第一个参数为加密(ENCRYPT_MODE)或者解密(DECRYPT_MODE)操作，第二个参数为生成的AES密钥
            cipher.init(Cipher.DECRYPT_MODE, skey);
            //把密文字符串转回密文字节数组
            byte[] encodeContent = Base64.decodeBase64(encryptStr);
            //密码器解密数据
            byte[] byteContent = cipher.doFinal(encodeContent);
            //将解密后的数据转换为字符串返回
            return new String(byteContent, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }

}
