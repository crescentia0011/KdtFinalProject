package com.weple.cloud.notification.service.mail;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@ConditionalOnBean(JavaMailSender.class)
@RequiredArgsConstructor
public class NotificationMailServiceImpl implements NotificationMailService {

    private final JavaMailSender mailSender;

    @Value("${notification.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${spring.mail.username:}")
    private String fromAddress;

    @Override
    public void sendAlarmMail(String toEmail, String alarmTag, String alarmContent) {
    	
        if (!mailEnabled) {
            log.debug("notification.mail.enabled=false 라 이메일 발송을 건너뜁니다. to={}", toEmail);
            return;
        }
        if (toEmail == null || toEmail.isBlank()) {
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        if (fromAddress != null && !fromAddress.isBlank()) {
            message.setFrom(fromAddress);
        }
        message.setTo(toEmail);
        message.setSubject("[WEPLE] " + alarmTag + " 알림");
        message.setText(alarmContent);

        mailSender.send(message);
    }
}