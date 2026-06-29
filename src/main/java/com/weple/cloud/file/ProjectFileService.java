package com.weple.cloud.file;

import java.util.List;

public interface ProjectFileService {
	// -------------------------------파일관리------------------------------
	// 전체조회
	public List<ProjectFileVO> findProjectFileAll();
	
	// 상세조회
	public ProjectFileVO findProjectFileInfo(String fileId);
	
	// 등록
	public String addProjectFile(ProjectFileVO projectFileVO);
	
	// 삭제
	public long removeProjectFile(String fileId);
	
	
	// -------------------------------파일 버전------------------------------
  	// 전체조회
	List<ProjectFileVersionsVO> findProjectFileVersionAll(String fileId);
    
    // 상세조회
	public ProjectFileVersionsVO findProjectFileVersionInfo(String versionId);
    
    // 등록
	public String addProjectFileVersion(ProjectFileVersionsVO projectFileVersionsVO);
    
    // 삭제
	public long removeProjectFileVersion(String versionId);
}
