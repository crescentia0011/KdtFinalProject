package com.weple.cloud.auth.service;

public interface SignupService {

    // 회원가입 입력값을 검증하고 USERS 테이블에 가입 대기 계정을 생성합니다.
    void signup(SignupRequestVO request);
}
