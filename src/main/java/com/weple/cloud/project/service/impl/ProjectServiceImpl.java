package com.weple.cloud.project.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.weple.cloud.project.mapper.ProjectMapper;
import com.weple.cloud.project.service.ProjectService;
import com.weple.cloud.project.service.ProjectVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {
	private final ProjectMapper projectMapper;
	
	@Override
	public List<ProjectVO> findAll(String keyword){
		return projectMapper.selectAll(keyword);
	}

	@Override
	public ProjectVO findById(String projectId) {
		return projectMapper.selectById(projectId);
	}
}
