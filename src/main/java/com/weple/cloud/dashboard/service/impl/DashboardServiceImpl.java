package com.weple.cloud.dashboard.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.weple.cloud.dashboard.mapper.DashboardMapper;
import com.weple.cloud.dashboard.service.DashboardProjectDTO;
import com.weple.cloud.dashboard.service.DashboardService;
import com.weple.cloud.dashboard.service.WorkLog2VO;
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
    public List<WorkLog2VO> getRecentActivities(com.weple.cloud.auth.service.LoginUserVO loginUser, String projectId, int limit) {
        List<Long> allowedProjectIds = new ArrayList<>();
        boolean isAdmin = Integer.valueOf(1).equals(loginUser.getOwnerYn()) 
                       || Integer.valueOf(1).equals(loginUser.getAdminYn());

        if (isAdmin) {
            // 관리자이면서 특정 프로젝트를 선택한 경우
            if (projectId != null && !projectId.trim().isEmpty()) {
                allowedProjectIds.add(Long.parseLong(projectId));
            }
            // projectId가 없으면 빈 리스트 상태로 매퍼에 전달 -> XML에서 전체 조회
        } else {
            // 일반 사용자 권한 체크
            String myUserCode = loginUser.getUserCode();
            List<ProjectVO> myProjects = projectService.findAllByMember(myUserCode, "", 0, Integer.MAX_VALUE);
            
            if (projectId != null && !projectId.trim().isEmpty()) {
                long reqProjId = Long.parseLong(projectId);
                // 요청한 프로젝트에 본인이 참여 중인지 확인
                boolean isParticipant = myProjects.stream().anyMatch(p -> p.getProjectId() == reqProjId);
                if (isParticipant) {
                    allowedProjectIds.add(reqProjId);
                } else {
                    allowedProjectIds.add(-1L); // 권한 없는 프로젝트 방어 코드
                }
            } else {
                // 특정 프로젝트 선택 안 했을 시, 본인이 속한 모든 프로젝트 목록 추가
                for (ProjectVO p : myProjects) {
                    allowedProjectIds.add(p.getProjectId());
                }
                if (allowedProjectIds.isEmpty()) {
                    allowedProjectIds.add(-1L);
                }
            }
        }

        return dashboardMapper.selectRecentActivities(allowedProjectIds, limit);
    }
    
}