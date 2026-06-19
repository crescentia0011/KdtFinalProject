package com.weple.cloud.auth.service;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginUserResponse {

    // 사용자 고유 코드
    private String userCode;

    // 소속 기업 ID
    private Long companyId;

    // 로그인 아이디
    private String loginId;

    // 사용자 이름
    private String userName;

    // 이메일
    private String email;

    // 계정 상태 코드
    private String status;

    // 기업 최고관리자 여부
    private Integer ownerYn;

    // 부여받은 관리자 여부
    private Integer adminYn;
}
