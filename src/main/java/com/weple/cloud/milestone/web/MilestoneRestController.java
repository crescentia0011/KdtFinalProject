package com.weple.cloud.milestone.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.weple.cloud.auth.service.LoginUserDetails;
import com.weple.cloud.auth.service.LoginUserVO;
import com.weple.cloud.milestone.service.MilestoneService;
import com.weple.cloud.milestone.service.MilestoneVO;
import com.weple.cloud.repository.service.RepositoryService; // [추가] 권한 조회를 위해 주입

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/project/milestone/api")
public class MilestoneRestController {

    // [추가] 일감 편집 관련 권한 코드 상수 정의
    private static final String PERMISSION_TASK_UPDATE = "k3_edit";
    private static final String PERMISSION_TASK_MYUPDATE = "k3_myedit";

    private final MilestoneService milestoneService;
    private final RepositoryService repositoryService; // [추가] 프로젝트별 팀원 권한 조회 서비스

    /**
     * 마일스톤 상세페이지 - 연결된 일감 실시간 업데이트 (보안 권한 체크 반영)
     */
    @PostMapping("/update-task-mapping")
    public ResponseEntity<Map<String, Object>> updateTaskMapping(
            @AuthenticationPrincipal LoginUserDetails loginUser, // [추가] 인증 정보 객체 주입
            @RequestParam("projectId") Long projectId,
            @RequestParam("milestoneId") Long milestoneId,
            @RequestParam(value = "taskIds", required = false) List<String> taskIds) {
        
        Map<String, Object> response = new HashMap<>();
        
        // 1. 프로젝트 내 유저 권한 목록 조회
        Set<String> permissionCodes = findMilestonePermissionCodes(loginUser, projectId);
        
        // 2. 일감 전체 편집(k3_edit)과 내 일감 편집(k3_myedit)이 '둘 다 없는' 경우 권한 예외 처리
        if (!hasMilestonePermission(permissionCodes, PERMISSION_TASK_UPDATE) && 
            !hasMilestonePermission(permissionCodes, PERMISSION_TASK_MYUPDATE)) {
            
            response.put("success", false);
            response.put("message", "해당 프로젝트의 일감 연결 및 편집 권한이 없습니다.");
            
            // 비동기 API 권한 부족이므로 HTTP 403 Forbidden 코드로 리턴 처리
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
        
        try {
            // 3. 서비스 메서드 시그니처에 맞게 MilestoneVO 객체 생성 및 데이터 세팅
            MilestoneVO milestoneVO = new MilestoneVO();
            milestoneVO.setProjectId(projectId);
            milestoneVO.setMilestoneId(milestoneId);
            
            // 4. 기존 일감 매핑 수정 서비스 로직 호출
            milestoneService.modifyMilestoneTasks(milestoneVO, taskIds);
            
            // 5. 성공 피드백 구성
            response.put("success", true);
            response.put("message", "일감 연결 상태가 성공적으로 반영되었습니다.");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            
            response.put("success", false);
            response.put("message", "일감 연결 중 오류가 발생했습니다: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /* ================= 팀원 양식 맞춤 권한 체크 유틸리티 메서드 ================= */

    /**
     * 기업 최고관리자/관리자면 전체 허용, 일반 사용자면 프로젝트별 권한 목록 조회
     */
    private Set<String> findMilestonePermissionCodes(LoginUserDetails loginUser, Long projectId) {
        if (loginUser == null || loginUser.getLoginUser() == null) {
            return Set.of();
        }
        LoginUserVO user = loginUser.getLoginUser();
        if (isCompanyManager(user)) {
            // 최고관리자 및 시스템 관리자는 모든 권한 통과 프리패스 처리
            return Set.of(PERMISSION_TASK_UPDATE, PERMISSION_TASK_MYUPDATE);
        }
        return repositoryService.findProjectPermissionCodes(user.getUserCode(), projectId);
    }

    /**
     * 조회된 권한 목록에 필요한 권한 코드가 있는지 확인
     */
    private boolean hasMilestonePermission(Set<String> permissionCodes, String permissionCode) {
        return permissionCodes != null && permissionCodes.contains(permissionCode);
    }

    /**
     * 회사 최고관리자(Owner) 또는 시스템 관리자(Admin) 여부 확인
     */
    private boolean isCompanyManager(LoginUserVO user) {
        return Integer.valueOf(1).equals(user.getOwnerYn()) || Integer.valueOf(1).equals(user.getAdminYn());
    }
}
