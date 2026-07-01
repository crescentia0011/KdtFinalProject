package com.weple.cloud.notification.service.mail;

/**
 * 마이페이지(Dv-044) "이메일 알림 수신 연동" 설정이 켜진 사용자에게
 * 알림 내용을 이메일로 발송한다.
 */
public interface NotificationMailService {

    void sendAlarmMail(String toEmail, String alarmTag, String alarmContent);
}