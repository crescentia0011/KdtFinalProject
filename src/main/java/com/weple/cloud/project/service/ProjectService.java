package com.weple.cloud.project.service;

import java.util.List;

public interface ProjectService {
	// 전체 목록 조회(페이징)
	public List<ProjectVO> findAll(String keyword, int offset, int pageSize);
	
	// 전체 목록 조회(페이징x)
	public List<ProjectVO> findAll(String keyword);
	
	// 전체 건수 조회
	public int countAll(String keyword);
	
	// 단건 조회
	public ProjectVO findById(String projectId);
	
	// 모듈명 목록 조회
	public List<String> findModuleNames(Long projectId);
	
	// 설정 페이지 - 프로젝트 설정 정보 조회
	public ProjectVO findSettingById(Long projectId);
	
	// 설정 페이지 - 프로젝트 설정 저장
	public void saveProjectSetting(ProjectVO vo);
}
