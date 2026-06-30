package com.weple.cloud.project.service;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class ProjectMemberVO {

    // members 테이블
    private Long memberId; 
    private Long projectId;
    private String userCode;     

    // users 테이블 JOIN
    private String userName;   
    private String email;     
    private String profileImage;  

    // member_roles 테이블 JOIN
    private Long roleId;   
    private String roleName;    

    // 화면 표시용 순번
    private Integer rowNum;
    
    // 그룹 조회용
    private Long groupId;
    private String groupName;
    
    // 그룹 내 사용자 조회용 (이미 프로젝트 소속 여부)
    private Boolean isAlreadyMember;
}