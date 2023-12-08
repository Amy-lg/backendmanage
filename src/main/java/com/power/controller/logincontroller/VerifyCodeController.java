package com.power.controller.logincontroller;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.ShearCaptcha;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 验证码工具类
 * @author cyk
 * @since 2023/11
 */
@RestController
@RequestMapping("/api/verify")
public class VerifyCodeController {

    public static final Map<String, String> verifyMap = new HashMap<>();


    /**
     * 生成验证码方式一：生成扭曲干扰验证码(shearCaptcha)
     * 图片格式验证码
     * @param request
     * @param response
     */
    @GetMapping("/shearCaptcha")
    public void verifyShearCaptcha(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("image/jpeg");
        response.setHeader("pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        // 定义图形验证码的长、宽、验证码字符数、干扰线宽度
        ShearCaptcha shearCaptcha = CaptchaUtil.createShearCaptcha(400, 100, 5, 6);
        // 图形验证码写出，可以写出到文件或者写出到流
        ServletOutputStream opt = response.getOutputStream();
        shearCaptcha.write(opt);
        // 获取验证码中的文字内容，存储到session中
        String code = shearCaptcha.getCode();
        verifyMap.put("captVerifyCode", code);
        request.getSession().setAttribute("captVerifyCode", shearCaptcha.getCode());
        opt.flush();
        opt.close();
    }


    /**
     * 验证前端传入的验证码是否正确
     * @param captchaCode
     * @param request
     * @return
     */
    @GetMapping("/checkCode")
    public boolean getCheckCaptcha(@RequestParam("captchaCode") String captchaCode, HttpServletRequest request) {

        try {
            // 获取会话域中的验证码信息，并转为小写
            String sessionCaptchaCode = String.valueOf(request.getSession().getAttribute("verifyCode")).toLowerCase();
            // 将前端传过来的captchaCode转为小写，
            String checkCaptchaCode = captchaCode.toLowerCase();
            // 验证
            return !"".equals(sessionCaptchaCode) && !"".equals(checkCaptchaCode)
                    && sessionCaptchaCode.equals(checkCaptchaCode);
        }catch (Exception e) {
            return false;
        }
    }
}
