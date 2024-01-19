package com.power.dealMessage.email.service.impl;

import com.power.common.constant.ProStaConstant;
import com.power.dealMessage.email.service.EmailService;
import com.power.entity.fault.FaultTrackingEntity;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 邮件推送服务实现层
 * @author cyk
 * @since 2024/1
 */
@Service
public class EmailServiceImpl implements EmailService {


    /**
     * 简单邮件发送
     * @param javaMailSender
     * @param from
     * @param faultDataList
     * @return 发送结果
     */
    @Override
    public void sendMsgByEmail(JavaMailSenderImpl javaMailSender, String from,
                                 List<FaultTrackingEntity> faultDataList) {

        try {
            int compare = 0;
            // 当前系统时间
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String currentFormatDate = dateFormat.format(new Date());
            String expRepairDate = null;
            if (faultDataList != null && faultDataList.size() > 0) {

                // 简单邮件发送
                SimpleMailMessage message = new SimpleMailMessage();
                // 设置邮件主题
                message.setSubject(ProStaConstant.EXP_REMINDER_SUB);
                // 设置发送人
                message.setFrom(from);
                // 设置接收人（接收人的获取需要从故障表字段信息中获取）
                message.setTo("18867119065@139.com");
                // 设置邮件发送日期
                message.setSentDate(new Date());
                for (FaultTrackingEntity fault : faultDataList) {
                    String progressStatus = fault.getProgressStatus();
                    if (progressStatus != null && !"".equals(progressStatus) &&
                            !ProStaConstant.FIXED.equals(progressStatus)) {

                        // 获取预计修复日期
                        expRepairDate = fault.getExpRepairDate();
                        compare = expRepairDate.compareToIgnoreCase(currentFormatDate);
                        // 预计修复日期<=当前日期，需要推送邮件提示
                        if (compare == -1 || compare == 0) {
                            // 设置邮件内容
                            message.setText("项目:" + fault.getProjectName()+"。即将过期或已超出预计修复日期，请尽快修复！\n"+
                                    "项目处理状态:" + fault.getDialStatus() + "\n" +
                                    "备注：" + fault.getNotes());
                            javaMailSender.send(message);
                        }
                    }
                }
            }
        } catch (MailException e) {
            e.printStackTrace();
        }
    }


}
