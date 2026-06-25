package com.weple.cloud.project.service;

import java.util.List;

public interface ProjectService {
	public List<ProjectVO> findAll(String keyword, int offset, int pageSize);
	public List<ProjectVO> findAll(String keyword); // 전체조회
	int countAll(String keyword);
	public ProjectVO findById(String projectId);
	public List<String> findModuleNames(Long projectId);
}
