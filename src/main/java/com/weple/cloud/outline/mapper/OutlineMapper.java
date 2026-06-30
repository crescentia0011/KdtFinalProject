package com.weple.cloud.outline.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.weple.cloud.milestone.service.TaskGroupStatVO;
import com.weple.cloud.outline.service.ProjectGroupMemberDTO;
import com.weple.cloud.outline.service.ProjectProgressDTO;
import com.weple.cloud.project.service.ProjectVO;

public interface OutlineMapper {
	
	// 프로젝트 조회
	ProjectVO selectProjectById(Long projectId);
	
	// 구성원 조회
	List<ProjectGroupMemberDTO> selectProjectMembersByGroup(Long projectId);
	
	// 프로젝트 전체 요약 (시간 + 총 진척도)
    ProjectProgressDTO selectProjectProgressSummary(@Param("projectId") Long projectId);

    // 4대 기준 그룹 스탯
    List<TaskGroupStatVO> selectTaskStatusStats(@Param("projectId") Long projectId);
    List<TaskGroupStatVO> selectTaskPriorityStats(@Param("projectId") Long projectId);
    List<TaskGroupStatVO> selectTaskTypeStats(@Param("projectId") Long projectId);
    List<TaskGroupStatVO> selectTaskManagerStats(@Param("projectId") Long projectId);
}
