package com.weple.cloud.project.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.weple.cloud.project.service.ProjectVO;

@Mapper
public interface ProjectMapper {
	public List<ProjectVO> selectAll(String keyword);
	
	public ProjectVO selectById(String projectId);
}
