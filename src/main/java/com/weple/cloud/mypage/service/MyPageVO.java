package com.weple.cloud.mypage.service;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class MyPageVO {

    // 사용자 식별값
    private String userCode;

    // 로그인 아이디
    private String loginId;

    // 사용자 이름
    private String userName;

    // 이메일 계정
    private String email;

    // 소속 부서명
    private String groupName;

    // 직급/역할 표시명
    private String roleName;

    // 프로필 이미지 경로(예: /uploads/profile/xxx.png) USERS.PROFILE_IMAGE, 없으면 null
    private String profileImage;

    // 실시간 웹 알림 수신 여부
    private String webNotificationYn;

    // 이메일 알림 수신 연동 여부
    private String emailNotificationYn;

    // 알림 수신 범위
    private String notificationArea;
}