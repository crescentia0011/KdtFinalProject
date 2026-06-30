package com.weple.cloud.repository.service;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RepositoryVO {

    // DB에서 발급하는 저장소 식별자
    private String repositoryId;

    // 저장소별 커밋 자동 처리 설정을 가리키는 관리 설정 ID
    private Long repositoryManageId;

    // 로그인한 사용자가 속한 회사 ID
    private Long companyId;

    // 저장소가 연결될 프로젝트 ID
    private Long projectId;

    // 목록과 상세 화면에 표시할 저장소 이름
    private String repositoryName;

    // GitHub API 조회와 외부 이동에 사용할 저장소 주소
    private String repositoryUrl;

    // 프로젝트의 대표 저장소 여부. Y: 주 저장소, N: 일반 저장소
    private String mainYn;

    // 기존 주 저장소를 다른 저장소로 교체한다는 사용자 확인값입니다. DB에는 저장하지 않습니다.
    private boolean mainChangeConfirmed;

    // 저장소 등록 일시
    // 저장소 관리 설정 행을 만들 때 함께 저장할 커밋 연결 기본 설정값
    private String commitAutoYn;
    private String commitTextYn;
    private String taskKeyword;

    private Date createdAt;
}
