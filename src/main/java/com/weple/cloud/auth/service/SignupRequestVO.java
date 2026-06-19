package com.weple.cloud.auth.service;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequestVO {

    // 회원가입 시 DB에서 발급되는 사용자 고유 코드
    private String userCode;

    // 가입할 회사의 식별 코드
    private String companyCode;

    // 회사 코드로 조회한 회사 ID
    private Long companyId;

    // 로그인할 때 사용할 아이디
    private String loginId;

    // 사용자 이름
    private String userName;

    // 사용자 이메일
    private String email;

    // 사용자 연락처
    private String phoneNumber;

    // 로그인할 때 사용할 원본 비밀번호
    private String password;

    // DB에 저장할 BCrypt 암호화 비밀번호
    private String encodedPassword;

    // 비밀번호 오입력을 확인하기 위한 값
    private String passwordConfirm;
}
