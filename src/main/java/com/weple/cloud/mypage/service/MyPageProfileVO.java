package com.weple.cloud.mypage.service;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class MyPageProfileVO {

    // 사용자 식별값
    private String userCode;

    // 사용자 이름
    private String userName;

    // 소속 부서명
    private String groupName;

    // 직급/역할 표시명
    private String roleName;

    // 이메일 계정
    private String email;

    // 현재 적용 중인 프로필 사진 경로
    private String profileImage;
}