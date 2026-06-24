package com.weple.cloud.system.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.weple.cloud.system.service.SystemProjectVO;

@Mapper
public interface SystemProjectMapper {
	// 프로젝트 기본 정보 등록
	public int insertInfo(SystemProjectVO systemProjectVO);
	// 프로젝트 모듈 매핑 데이터 일괄 등록
	public int insertModuleMapping(SystemProjectVO systemProjectVO);
	// 식별자 중복 체크
	int countByIdentifier(String projectIdentifier);
	List<String> selectModuleNames(Long projectId);
	
	// 프로젝트 목록
	List<SystemProjectVO> selectProjectList(SystemProjectVO vo);
    int selectProjectCount(SystemProjectVO vo);
	
    // 프로젝트 수정
    SystemProjectVO selectProjectById(Long projectId);
    int updateProject(SystemProjectVO projectVO);
    int deleteModuleMapping(Long projectId);
    
	// 프로젝트 삭제
	int deleteProject(String projectId);
}
