package com.weple.cloud.system.service;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserManagementCreateVO {

    // 화면에서 받지 않고 로그인한 관리자의 회사 정보로 채울 회사 번호
    private Long companyId;

    // usr-yyyymmdd-번호 형식으로 서비스에서 발급할 사용자 코드
    private String userCode;

    // 신규 사용자가 로그인할 때 사용할 아이디
    private String loginId;

    // 암호화 전 비밀번호와 확인값입니다. 저장 전 password에는 암호화된 값을 다시 담음
    private String password;
    private String passwordConfirm;

    // 신규 사용자 기본 정보
    private String userName;
    private String email;
    private String phoneNumber;

    // 관리자 권한 부여 여부 최고관리자는 화면에서 만들지 않음
    private Integer adminYn;

    // 사용자의 웹/이메일 알림 발송 여부
    private String webNotificationYn;
    private String emailNotificationYn;
}
