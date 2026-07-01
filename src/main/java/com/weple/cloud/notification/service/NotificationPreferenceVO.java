package com.weple.cloud.notification.service;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * NotificationServiceImpl.create() 에서 알림을 만들기 직전, 수신자(userCode)의
 * 알림 관련 설정을 한 번에 조회하기 위한 VO. 전부 USERS 테이블의 기존 컬럼이며,
 * 새 컬럼 추가 없이 마이페이지(Dv-044)에서 저장한 값을 그대로 읽어온다.
 */
@NoArgsConstructor
@Getter
@Setter
@ToString
public class NotificationPreferenceVO {

    private String userCode;

    // 이메일 알림 발송 대상 주소
    private String email;

    // "all"/"mine"/"mention" (마이페이지 알림 수신 범위 설정)
    private String notificationArea;

    private String emailNotificationYn;
}