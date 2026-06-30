package com.weple.cloud.repository.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.weple.cloud.repository.service.RepositoryCommitLogVO;
import com.weple.cloud.repository.service.RepositoryManageSettingVO;
import com.weple.cloud.repository.service.RepositoryVO;
import com.weple.cloud.repository.service.RepositoryTaskLinkInfo;

@Mapper
public interface RepositoryMapper {

    // 프로젝트에 등록된 저장소 건수 조회함
    int countRepositoriesByProjectId(@Param("projectId") Long projectId);

    // 같은 회사, 같은 프로젝트 안에서 저장소명 또는 GitHub 주소 중복 확인함
    int countRepositoryByNameOrUrl(@Param("companyId") Long companyId,
                                   @Param("projectId") Long projectId,
                                   @Param("repositoryName") String repositoryName,
                                   @Param("repositoryUrl") String repositoryUrl);

    // 수정 중인 저장소 자신을 제외하고 저장소명 중복 확인함
    int countRepositoryNameExcept(@Param("companyId") Long companyId,
                                  @Param("projectId") Long projectId,
                                  @Param("repositoryName") String repositoryName,
                                  @Param("repositoryId") String repositoryId);

    // 회사와 프로젝트에 속한 저장소 목록 조회함
    List<RepositoryVO> selectRepositories(@Param("companyId") Long companyId, @Param("projectId") Long projectId);

    // 다른 회사, 다른 프로젝트 저장소를 조회하지 못하도록 키 조건으로 조회함
    RepositoryVO selectRepository(@Param("companyId") Long companyId,
                                  @Param("projectId") Long projectId,
                                  @Param("repositoryId") String repositoryId);

    // 프로젝트에서 현재 주 저장소로 지정된 1건 조회함
    RepositoryVO selectMainRepository(@Param("companyId") Long companyId, @Param("projectId") Long projectId);

    // 커밋 메시지에서 인식한 일감 코드 중 실제 프로젝트에 존재하는 일감 제목 조회함
    List<RepositoryTaskLinkInfo> selectTaskLinkInfos(@Param("projectId") Long projectId,
                                                     @Param("taskIds") List<String> taskIds);

    // 사용자가 해당 프로젝트에서 가진 권한 코드 전체 조회함
    List<String> selectProjectPermissionCodes(@Param("userCode") String userCode,
                                              @Param("projectId") Long projectId);

    // 이미 수집된 커밋 로그에 저장된 일감 연결값 조회함
    List<RepositoryCommitLogVO> selectCommitLogsByHashes(@Param("repositoryId") String repositoryId,
                                                         @Param("commitHashes") List<String> commitHashes);

    // 처음 보는 커밋만 commit_logs에 저장함
    int mergeCommitLogIfAbsent(RepositoryCommitLogVO commitLog);

    // 새 주 저장소 등록 전 기존 주 저장소 표시 해제함
    int clearMainRepository(@Param("companyId") Long companyId, @Param("projectId") Long projectId);

    // 저장소별 관리 설정을 먼저 만들고 생성된 ID를 RepositoryVO에 담음
    int insertRepositoryManageSetting(RepositoryVO repository);

    // 회사별 저장소 커밋 연결 설정 1건 조회함
    RepositoryManageSettingVO selectRepositoryManageSetting(@Param("companyId") Long companyId);

    // 회사 설정 행 존재 여부 확인함
    int countRepositoryManageSettings(@Param("companyId") Long companyId);

    // 회사 설정 행이 없을 때 최초 생성함
    int insertCompanyRepositoryManageSetting(RepositoryManageSettingVO setting);

    // 회사별 저장소 설정값 일괄 수정함
    int updateRepositoryManageSettings(RepositoryManageSettingVO setting);

    // 저장소 등록함
    int insertRepository(RepositoryVO repository);

    // 저장소명과 주 저장소 여부 수정함
    int updateRepository(RepositoryVO repository);

    // 저장소 행 삭제함
    int deleteRepository(@Param("companyId") Long companyId,
                         @Param("projectId") Long projectId,
                         @Param("repositoryId") String repositoryId);

    // 저장소와 연결된 관리 설정 행 삭제함
    int deleteRepositoryManageSetting(@Param("repositoryManageId") Long repositoryManageId);
}
