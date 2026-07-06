package com.weple.cloud.dashboard.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.weple.cloud.dashboard.mapper.DashboardMapper;
import com.weple.cloud.dashboard.service.DashboardProjectDTO;
import com.weple.cloud.dashboard.service.DashboardService;
import com.weple.cloud.history.worklog.service.WorkLogVO;
import com.weple.cloud.project.service.ProjectService;
import com.weple.cloud.project.service.ProjectVO;
import com.weple.cloud.task.service.TaskVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final DashboardMapper dashboardMapper;
    private final ProjectService projectService;

    @Override
    public List<TaskVO> getTasksDueWithinAWeek(String userCode) {
        return dashboardMapper.selectTasksDueWithinAWeek(userCode);
    }
    
    @Override
    public List<DashboardProjectDTO> getProjectsForDashboard(String userCode, boolean isManager) {
        if (isManager) {
            // 🌟 최고 관리자라면 전체 프로젝트 통계 조회
            return dashboardMapper.selectAllProjectsForDashboard();
        } else {
            // 일반 사용자라면 참여 중인 프로젝트 통계만 조회
            return dashboardMapper.selectProjectsByMember(userCode);
        }
    }
    
    
    
    @Override
    public List<WorkLogVO> getRecentActivities(String userCode, String projectId) {
        Map<String, Object> params = new HashMap<>();
        
        if (projectId != null && !projectId.isEmpty()) {
            params.put("projectId", projectId);
        } else {
            // 내 참여 프로젝트 전체 목록 조회하여 ID만 추출
            List<ProjectVO> myProjects = projectService.findAllByMember(userCode, "", 0, Integer.MAX_VALUE);
            if (myProjects.isEmpty()) {
                return List.of(); // 참여중인 프로젝트가 없으면 조회 생략
            }
            
            List<Long> projectIds = myProjects.stream()
                    .map(ProjectVO::getProjectId)
                    .collect(Collectors.toList());
            params.put("projectIds", projectIds);
        }

        return dashboardMapper.selectRecentActivities(params);
    }
    
}