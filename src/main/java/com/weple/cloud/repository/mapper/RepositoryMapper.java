package com.weple.cloud.repository.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.weple.cloud.repository.service.RepositoryVO;

@Mapper
public interface RepositoryMapper {

    // 프로젝트에 등록된 저장소 건수를 조회
    int countRepositoriesByProjectId(@Param("projectId") Long projectId);

    // 같은 회사·프로젝트 안에서만 저장소명 또는 GitHub 주소 중복을 확인
    int countRepositoryByNameOrUrl(@Param("companyId") Long companyId,
                                   @Param("projectId") Long projectId,
                                   @Param("repositoryName") String repositoryName,
                                   @Param("repositoryUrl") String repositoryUrl);

    // 같은 회사·프로젝트에서 수정 대상 자신을 제외한 저장소명 중복을 확인
    int countRepositoryNameExcept(@Param("companyId") Long companyId,
                                  @Param("projectId") Long projectId,
                                  @Param("repositoryName") String repositoryName,
                                  @Param("repositoryId") String repositoryId);

    // 회사와 프로젝트에 속한 저장소 목록을 조회
    List<RepositoryVO> selectRepositories(@Param("companyId") Long companyId, @Param("projectId") Long projectId);

    // 다른 회사·프로젝트 저장소를 조회하지 못하도록 세 조건으로 단건을 조회
    RepositoryVO selectRepository(@Param("companyId") Long companyId,
                                  @Param("projectId") Long projectId,
                                  @Param("repositoryId") String repositoryId);

    // 프로젝트에서 현재 주 저장소로 지정된 한 건을 조회
    RepositoryVO selectMainRepository(@Param("companyId") Long companyId, @Param("projectId") Long projectId);

    // 새 주 저장소 등록 전 기존 주 저장소 표시를 해제
    int clearMainRepository(@Param("companyId") Long companyId, @Param("projectId") Long projectId);

    // 저장소별 기본 관리 설정을 먼저 만들고 생성된 ID를 RepositoryVO에 담는다.
    int insertRepositoryManageSetting(RepositoryVO repository);

    // 기본 관리 설정 ID를 포함해 실제 저장소 등록 정보를 저장
    int insertRepository(RepositoryVO repository);

    // GitHub 주소는 그대로 두고 저장소명과 주 저장소 여부만 변경
    int updateRepository(RepositoryVO repository);

    // 저장소를 먼저 제거한 뒤 연결된 관리 설정을 정리
    int deleteRepository(@Param("companyId") Long companyId,
                         @Param("projectId") Long projectId,
                         @Param("repositoryId") String repositoryId);

    int deleteRepositoryManageSetting(@Param("repositoryManageId") Long repositoryManageId);
}
