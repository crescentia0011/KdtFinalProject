package com.weple.cloud.repository.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface RepositoryService {

    // 관리자가 입력한 GitHub 저장소를 프로젝트에 등록함
    void registerRepository(RepositoryVO repository);

    // 기존 저장소의 표시 이름과 주 저장소 여부를 변경함
    void updateRepository(RepositoryVO repository);

    // 확인용 저장소명이 일치할 때만 저장소 삭제함
    void deleteRepository(Long companyId, Long projectId, String repositoryId, String confirmationName);

    // 프로젝트에 등록된 저장소가 있는지 확인함
    boolean hasRepository(Long projectId);

    // 현재 회사와 프로젝트에 등록된 저장소 목록 조회함
    List<RepositoryVO> findRepositories(Long companyId, Long projectId);

    // 목록에서 선택한 저장소 1건 조회함
    RepositoryVO findRepository(Long companyId, Long projectId, String repositoryId);

    // 프로젝트 주 저장소 조회함
    RepositoryVO findMainRepository(Long companyId, Long projectId);

    // 커밋 메시지에서 인식한 일감 코드의 실제 일감 제목 조회함
    Map<String, String> findTaskTitles(Long projectId, List<String> taskIds);

    // 사용자가 해당 프로젝트에서 가진 권한 코드 전체 조회함
    Set<String> findProjectPermissionCodes(String userCode, Long projectId);

    // commit_logs에 저장된 일감 연결값을 커밋 목록에 반영함
    void applyStoredCommitLinks(String repositoryId, List<GithubCommitInfo> commits);

    // 회사별 저장소 커밋 연결 설정 조회함
    RepositoryManageSettingVO findRepositoryManageSetting(Long companyId);

    // 관리 설정 화면에서 변경한 저장소 커밋 연결 설정 저장함
    void saveRepositoryManageSetting(RepositoryManageSettingVO setting);
}
