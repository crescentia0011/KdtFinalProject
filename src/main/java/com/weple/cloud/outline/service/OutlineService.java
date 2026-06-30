package com.weple.cloud.outline.service;

import java.util.List;

import com.weple.cloud.project.service.ProjectVO;

public interface OutlineService {
	
	ProjectVO getProjectById(Long projectId);
	
	List<ProjectGroupMemberDTO> selectProjectMembersByGroup(Long projectId);

	ProjectProgressDTO getProjectProgress(Long projectId);


}
