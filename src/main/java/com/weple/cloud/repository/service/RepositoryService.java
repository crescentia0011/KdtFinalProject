package com.weple.cloud.repository.service;

import java.util.List;

public interface RepositoryService {

    // 관리자가 입력한 GitHub 저장소를 프로젝트에 등록합니다.
    void registerRepository(RepositoryVO repository);

    // 상단 저장소 탭을 노출할 수 있도록 프로젝트의 등록 저장소 존재 여부를 확인합니다.
    boolean hasRepository(Long projectId);

    // 현재 회사와 프로젝트에 등록된 저장소 목록을 조회합니다.
    List<RepositoryVO> findRepositories(Long companyId, Long projectId);

    // 목록에서 선택한 저장소 한 건을 조회합니다.
    RepositoryVO findRepository(Long companyId, Long projectId, String repositoryId);
}
