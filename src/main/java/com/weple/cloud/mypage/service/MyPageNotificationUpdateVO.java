package com.weple.cloud.mypage.service;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class MyPageNotificationUpdateVO {

    // 수정 대상 사용자 식별값
    private String userCode;

    // 실시간 웹 알림 수신 여부
    private String webNotificationYn;

    // 이메일 알림 수신 연동 여부
    private String emailNotificationYn;

    // 알림 수신 범위
    private String notificationArea;
}