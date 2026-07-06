package com.weple.cloud.dashboard.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.weple.cloud.auth.service.LoginUserDetails;
import com.weple.cloud.dashboard.service.DashboardProjectDTO;
import com.weple.cloud.dashboard.service.DashboardService;
import com.weple.cloud.history.worklog.service.WorkLogVO;
import com.weple.cloud.task.service.TaskVO;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardRestController {

    private final DashboardService dashboardService;

    // D-7 마감 임박 일감 가져오기 API
    @GetMapping("/upcoming-tasks")
    public ResponseEntity<?> getUpcomingTasks(@AuthenticationPrincipal LoginUserDetails loginUser) {
        // 별도의 권한 검증 없이 바로 내 일감 조회 진행
        String userCode = loginUser.getLoginUser().getUserCode();
        List<TaskVO> tasks = dashboardService.getTasksDueWithinAWeek(userCode);
        
        return ResponseEntity.ok(tasks);
    }
    
    
 // 🌟 ProjectController에서 참조한 최고 관리자 판별 로직 추가
    private boolean isCompanyManager(com.weple.cloud.auth.service.LoginUserVO user) {
        return Integer.valueOf(1).equals(user.getOwnerYn())
            || Integer.valueOf(1).equals(user.getAdminYn());
    }
    
 // 내가 참여 중인 프로젝트 목록 가져오기 API (권한 검증 제외)
    @GetMapping("/my-projects")
    public ResponseEntity<?> getMyProjects(@AuthenticationPrincipal LoginUserDetails loginUser) {
        // 비로그인 방어 가드
        if (loginUser == null || loginUser.getLoginUser() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        
        com.weple.cloud.auth.service.LoginUserVO user = loginUser.getLoginUser();
        String userCode = user.getUserCode();
        
        // 🌟 참조 코드의 권한 체크 흐름 적용
        boolean isManager = isCompanyManager(user);
        
        // 권한 분기가 반영된 서비스 메서드 호출
        List<DashboardProjectDTO> projects = dashboardService.getProjectsForDashboard(userCode, isManager);
        
        return ResponseEntity.ok(projects);
    }
    
 // 최근 활동 내역 가져오기 API
    @GetMapping("/recent-activities")
    public ResponseEntity<?> getRecentActivities(
            @AuthenticationPrincipal LoginUserDetails loginUser,
            @RequestParam(required = false) String projectId) {
        
        String userCode = loginUser.getLoginUser().getUserCode();
        List<WorkLogVO> activities = dashboardService.getRecentActivities(userCode, projectId);
        
        return ResponseEntity.ok(activities);
    }
}

