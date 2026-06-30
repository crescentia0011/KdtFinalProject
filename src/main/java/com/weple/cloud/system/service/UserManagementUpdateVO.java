package com.weple.cloud.system.service;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserManagementUpdateVO {

    // 수정 대상 사용자 식별값입니다.
    private String userCode;

    // 사용자 기본정보 수정 화면에서 변경 가능한 항목입니다.
    private String loginId;
    private String userName;
    private String email;
    private String phoneNumber;
    private Integer groupId;

    // 기업 최고관리자가 사용자 수정 시 관리자 여부를 함께 변경할 때 사용합니다.
    private Integer adminYn;
}
