package com.power.dealMessage.email.service;

import com.power.entity.fault.FaultTrackingEntity;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.List;

public interface EmailService {


    /**
     * 简单邮件发送
     * @param javaMailSender
     * @param from
     * @param faultDataList
     * @return
     */
    void sendMsgByEmail(JavaMailSenderImpl javaMailSender,
                          String from,
                          List<FaultTrackingEntity> faultDataList);


}
