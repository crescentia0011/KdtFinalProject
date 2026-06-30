package com.weple.cloud.repository.service;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RepositoryManageSettingVO {

    // 저장소 관리 설정 PK 필요함
    private Long repositoryManageId;

    // 회사별 전역 설정 구분 필요함
    private Long companyId;

    // 커밋 내역 자동 수집 여부 저장함
    private String commitAutoYn;

    // 커밋 메시지에서 참조 키워드 형식 적용 여부 저장함
    private String commitTextYn;

    // 일감 연결에 사용할 참조 키워드 목록 저장함
    private String taskKeyword;
}
