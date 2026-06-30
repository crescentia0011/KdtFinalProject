package com.weple.cloud.system.service;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserManagementVO {

    // 상태 변경과 상세 조회에서 사용할 사용자 식별값입니다.
    private String userCode;
    private String loginId;
    private String userName;
    private String email;
    private String phoneNumber;
    private String profileImage;
    private String status;
    private String createdAt;
    private String lastLoginTime;
    private Integer groupId;
    private String groupName;
    private Integer ownerYn;
    private Integer adminYn;

    // 역할·권한 매핑 구현 전에는 USERS의 관리자 플래그로 임시 역할명을 계산합니다.
    private String roleName;
}
