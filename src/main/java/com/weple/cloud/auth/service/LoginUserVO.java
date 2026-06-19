package com.weple.cloud.auth.service;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 로그인 인증 시 USERS 테이블에서 조회한 사용자 정보를 담는 VO.
 * Spring Security 인증 객체(LoginUserDetails)를 만들 때 사용한다.
 */
@Getter
@Setter
@ToString
public class LoginUserVO {

    //사용자 고유 코드. USERS.USER_CODE
    private String userCode;

    // 소속 기업 ID. USERS.COMPANY_ID 
    private Long companyId;

    // 로그인 아이디. USERS.LOGIN_ID
    private String loginId;

    // BCrypt로 암호화된 비밀번호. USERS.PASSWORD 
    private String password;

    // 사용자 이름. USERS.USER_NAME 
    private String userName;

    // 이메일. USERS.EMAIL 
    private String email;

    // 연락처. USERS.PHONE_NUMBER 
    private String phoneNumber;

    
     // 계정 상태 코드. USERS.STATUS
     //a1: 승인대기, a2: 활성, a3: 비활성
     
    private String status;

    // 프로필 이미지 경로. USERS.PROFILE_IMAGE 
    private String profileImage;

    /**
     * 기업 최고관리자 여부. USERS.OWNER_YN
     * 1이면 운영자가 생성해 기업에 전달한 최고관리자 계정.
     */
    private Integer ownerYn;

    /**
     * 부여받은 관리자 여부. USERS.ADMIN_YN
     * 1이면 기업 최고관리자에게 관리자 권한을 부여받은 계정.
     */
    private Integer adminYn;

    // 웹 알림 수신 여부. USERS.WEB_NOTIFICATION_YN 
    private String webNotificationYn;

    // 이메일 알림 수신 여부. USERS.EMAIL_NOTIFICATION_YN 
    private String emailNotificationYn;

    // 알림 수신 범위. USERS.NOTIFICATION_AREA 
    private String notificationArea;

    // 최근 로그인 일시. USERS.LAST_LOGIN_TIME 
    private Date lastLoginTime;

    // 소속 그룹 ID. USERS.GROUP_ID 
    private Long groupId;
}