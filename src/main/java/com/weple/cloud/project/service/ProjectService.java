package com.weple.cloud.project.service;

import java.util.List;

public interface ProjectService {
	public List<ProjectVO> findAll(String keyword);
	
	public ProjectVO findById(String projectId);
}
