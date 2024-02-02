package com.power.dealMessage.email.controller;

import com.power.dealMessage.email.service.EmailService;
import com.power.entity.User;
import com.power.entity.fault.FaultTrackingEntity;
import com.power.service.UserService;
import com.power.service.faultservice.FaultTrackingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * 定时推送消息，通过邮件，以注解的形式
 * @author cyk
 * @since 2024/1
 */
@Controller
public class EmailController {

    @Autowired
    private EmailService emailService;

    // Spring框架提供的服务实现类来实现邮件推送
    @Autowired
    private JavaMailSenderImpl javaMailSender;

    // 故障追踪服务类(查询故障追踪表中数据处理日期)
    @Autowired
    private FaultTrackingService faultTrackingService;

    // 获取用户
    @Autowired
    private UserService userService;

    @Value("${spring.mail.username}")
    private String from;

    /**
     * 邮件消息推送
     * 0 0 8 * * ?  : 每天早晨8点执行;
     * 0 0/5 * * * ?  : 每5分钟执行一次;
     */
    @Scheduled(cron = "0 0 8 * * ? ")
    public void sendMsgByEmail() {
        List<User> userList = userService.list();
        // 获取故障数据信息集合
        List<FaultTrackingEntity> faultDataList = faultTrackingService.list();
        emailService.sendMsgByEmail(javaMailSender,from,faultDataList,userList);
    }

}
