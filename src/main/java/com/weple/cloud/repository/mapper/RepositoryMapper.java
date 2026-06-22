package com.weple.cloud.repository.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.weple.cloud.repository.service.RepositoryVO;

@Mapper
public interface RepositoryMapper {

    // 프로젝트에 등록된 저장소 건수를 조회합니다.
    int countRepositoriesByProjectId(@Param("projectId") Long projectId);

    // 같은 저장소명 또는 GitHub 주소가 이미 등록됐는지 확인합니다.
    int countRepositoryByNameOrUrl(@Param("repositoryName") String repositoryName,
                                   @Param("repositoryUrl") String repositoryUrl);

    // 회사와 프로젝트에 속한 저장소 목록을 조회합니다.
    List<RepositoryVO> selectRepositories(@Param("companyId") Long companyId, @Param("projectId") Long projectId);

    // 다른 회사·프로젝트 저장소를 조회하지 못하도록 세 조건으로 단건을 조회합니다.
    RepositoryVO selectRepository(@Param("companyId") Long companyId,
                                  @Param("projectId") Long projectId,
                                  @Param("repositoryId") String repositoryId);

    // 새 주 저장소 등록 전 기존 주 저장소 표시를 해제합니다.
    int clearMainRepository(@Param("companyId") Long companyId, @Param("projectId") Long projectId);

    // 저장소별 기본 관리 설정을 먼저 만들고 생성된 ID를 RepositoryVO에 담습니다.
    int insertRepositoryManageSetting(RepositoryVO repository);

    // 기본 관리 설정 ID를 포함해 실제 저장소 등록 정보를 저장합니다.
    int insertRepository(RepositoryVO repository);
}
