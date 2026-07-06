package com.weple.cloud.project.service;

import java.util.List;
import java.util.Set;

public interface ProjectService {
	// 전체 목록 조회(페이징)
	public List<ProjectVO> findAll(String keyword, int offset, int pageSize);
	
	// 전체 목록 조회(페이징x)
	public List<ProjectVO> findAll(String keyword);
	
	// 전체 건수 조회
	public int countAll(String keyword);
	
	// 단건 조회
	public ProjectVO findById(String projectId);
	
	// 관리에서 선택된 모듈 전체 목록 조회
	public List<String> findModuleNames(Long projectId);
	
	// 네비바 활성화된 모듈만 조회
	public List<String> findActiveModuleNames(Long projectId);
	
	// 설정 페이지 - 프로젝트 설정 정보 조회
	public ProjectVO findSettingById(Long projectId);
	
	// 설정 페이지 - 프로젝트 설정 저장
	public void saveProjectSetting(ProjectVO vo);
	
	// URL 접근 제어 - 모듈 활성화 여부
	public boolean isModuleActive(Long projectId, String moduleName);
	
	public Set<String> findProjectPermissionCodes(String userCode, Long projectId);
	public boolean isMember(String userCode, Long projectId);
	
	// 일반 사용자용 참여 프로젝트 목록 조회
	public List<ProjectVO> findAllByMember(String userCode, String keyword, int offset, int pageSize);
	public int countAllByMember(String userCode, String keyword);
	
	// 관리자용 - 소속 회사 전체 프로젝트 조회 (구성원 여부 무관)
	public List<ProjectVO> findAllByCompany(String companyId, String keyword, int offset, int pageSize);
	public int countAllByCompany(String companyId, String keyword);
	
	public Set<String> findAnyProjectPermissionCodes(String userCode);
}