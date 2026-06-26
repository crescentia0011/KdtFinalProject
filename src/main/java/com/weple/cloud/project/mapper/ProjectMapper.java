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
	
	// 모듈명 목록 조회
	public List<String> selectModuleNames(@Param("projectId") Long projectId);
	
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
}
