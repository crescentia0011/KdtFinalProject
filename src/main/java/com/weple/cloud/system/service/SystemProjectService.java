package com.weple.cloud.system.service;

import java.util.List;

public interface SystemProjectService {
	public int createProject(SystemProjectVO projectVO);
	boolean existsByIdentifier(String projectIdentifier);
	
	List<SystemProjectVO> selectProjectList(SystemProjectVO vo);
	int selectProjectCount(SystemProjectVO vo);
	
	SystemProjectVO selectProjectById(Long projectId);
	int updateProject(SystemProjectVO projectVO);
	
	int deleteProject(String projectId);
}
