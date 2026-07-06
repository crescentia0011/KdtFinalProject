package com.weple.cloud.project.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.weple.cloud.project.service.ProjectVO;

@Mapper
public interface ProjectMapper {
	// 전체 목록 조회(페이징)
	public List<ProjectVO> selectAll(
			 @Param("keyword") String keyword,
		     @Param("offset") int offset,
		     @Param("pageSize") int pageSize
		     );
	
	// 전체 목록 조회(페이징x)
	public List<ProjectVO> selectAllNoPage(@Param("keyword") String keyword);
	
	// 전체 건수 조회
	public int countAll(@Param("keyword") String keyword);
	
	// 단건 조회
	public ProjectVO selectById(String projectId);
	
	// 관리에서 선택된 모듈 전체 목록 조회
	public List<String> selectModuleNames(@Param("projectId") Long projectId);
	
	// 네비바 활성화된 모듈만 조회
	public List<String> selectActiveModuleNames(@Param("projectId") Long projectId);
	
	// 설정 페이지 - 프로젝트 설정 정보 단건 조회
	public ProjectVO selectSettingById(@Param("projectId") Long projectId);
	
	// 설정 페이지 - 프로젝트 기본 정보 수정
	public int updateProjectSetting(ProjectVO vo);
	
	// 설정 페이지 - 모듈 전체 삭제
	public int deleteModuleMapping(@Param("projectId") Long projectId);
	
	// 설정 페이지 - 모듈 단건 삽입
	public int insertModuleMapping(
            @Param("projectId")  Long projectId,
            @Param("moduleCode") String moduleCode);
	
	// 설정 페이지 - 활성여부 업데이트
	public int updateModuleActive(
            @Param("projectId")  Long projectId,
            @Param("moduleName") String moduleName,
            @Param("isActive")   String isActive);
	
	// URL 접근 제어 - 모듈 활성화 여부
	public int isModuleActive(@Param("projectId") Long projectId,
							  @Param("moduleName") String moduleName);
	
	public List<String> selectProjectPermissionCodes(
		    @Param("userCode") String userCode,
		    @Param("projectId") Long projectId);

	public boolean isMember(
		    @Param("userCode") String userCode,
		    @Param("projectId") Long projectId);

	public List<ProjectVO> selectAllByMember(
		    @Param("userCode") String userCode,
		    @Param("keyword") String keyword,
		    @Param("offset") int offset,
		    @Param("pageSize") int pageSize);

	public int countAllByMember(
		    @Param("userCode") String userCode,
		    @Param("keyword") String keyword);

	// 관리자용 - 소속 회사 전체 프로젝트 조회 (구성원 여부와 무관하게 회사 소속 프로젝트는 모두 조회)
	public List<ProjectVO> selectAllByCompany(
		    @Param("companyId") String companyId,
		    @Param("keyword") String keyword,
		    @Param("offset") int offset,
		    @Param("pageSize") int pageSize);

	public int countAllByCompany(
		    @Param("companyId") String companyId,
		    @Param("keyword") String keyword);
	
	// 특정 프로젝트에 한정하지 않고 이 사용자가 "어딘가에서든" 가진 프로젝트 관련 권한 코드 전체 조회
	// (프로젝트 생성 버튼 노출 여부 판단용)
	public List<String> selectAnyProjectPermissionCodes(@Param("userCode") String userCode);
}