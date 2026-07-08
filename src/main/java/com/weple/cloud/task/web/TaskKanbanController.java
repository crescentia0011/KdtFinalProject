package com.weple.cloud.task.web;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.weple.cloud.auth.service.LoginUserDetails;
import com.weple.cloud.project.service.ProjectService;
import com.weple.cloud.system.service.TaskTypeService;
import com.weple.cloud.system.service.TaskTypeVO;
import com.weple.cloud.task.mapper.TaskMapper;
import com.weple.cloud.task.service.TaskKanbanService;
import com.weple.cloud.task.service.TaskMemberVO;
import com.weple.cloud.task.service.TaskPermissionVO;
import com.weple.cloud.task.service.TaskStatusVO;
import com.weple.cloud.task.service.TaskVO;

import lombok.RequiredArgsConstructor;

// 칸반보드 전용 컨트롤러. 기존 TaskController는 건드리지 않기 위해 분리.
@Controller
@RequiredArgsConstructor
public class TaskKanbanController {

    private final TaskKanbanService taskKanbanService;
    private final TaskMapper taskMapper; // 조회 전용 재사용 (findMember, taskStatuses 등)
    private final ProjectService projectService;
    private final TaskTypeService taskTypeService;

    // 일감유형 뱃지 색상 순환용 (진한 단색 4가지를 순서대로 순환 적용)
    private static final String[] TYPE_COLOR_CLASSES = {
            "type-color-1", "type-color-2", "type-color-3", "type-color-4", "type-color-5",
            "type-color-6", "type-color-7", "type-color-8", "type-color-9", "type-color-10"
    };

    // 일감유형 이름 -> 뱃지 색상 클래스 매핑 (task_types position 순서 기준 4색 순환)
    private Map<String, String> buildTypeColorMap(List<TaskTypeVO> taskTypes) {
        Map<String, String> colorMap = new LinkedHashMap<>();
        for (int i = 0; i < taskTypes.size(); i++) {
            colorMap.put(taskTypes.get(i).getTypeName(), TYPE_COLOR_CLASSES[i % TYPE_COLOR_CLASSES.length]);
        }
        return colorMap;
    }

    @GetMapping("/project/task/kanban")
    public String projectTaskKanban(
            @RequestParam("projectId") Long pId,
            Model model,
            @AuthenticationPrincipal LoginUserDetails loginUser) {

        if (loginUser == null || loginUser.getLoginUser() == null) {
            return "weple/access-denide";
        }

        Integer ownerYn = loginUser.getLoginUser().getOwnerYn();
        Integer adminYn = loginUser.getLoginUser().getAdminYn();
        String userCode = loginUser.getLoginUser().getUserCode();
        boolean isAdminOrOwner = (ownerYn != null && ownerYn == 1) || (adminYn != null && adminYn == 1);

        List<TaskMemberVO> memberList = taskMapper.taskMembers(pId);
        boolean isProjectMember = memberList.stream()
                .anyMatch(member -> userCode.equals(member.getUserCode()));

        if (!isProjectMember && !isAdminOrOwner) {
            return "weple/access-denide";
        }

        List<TaskVO> list = taskKanbanService.findKanbanList(pId);
        List<TaskStatusVO> statusList = taskMapper.taskStatuses();
        TaskPermissionVO taskPerms = taskMapper.checkTaskPermissions(userCode, pId);

        // 상태(commonId)별로 미리 그룹핑 - 템플릿에서 SpEL 셀렉션 문법을 안 써도 되게
        Map<String, List<TaskVO>> tasksByStatus = new HashMap<>();
        for (TaskVO task : list) {
            tasksByStatus.computeIfAbsent(task.getTaskStatus(), k -> new java.util.ArrayList<>()).add(task);
        }

        // 일감유형 (뱃지 색상용, task_types 테이블에서 동적으로 가져옴)
        List<TaskTypeVO> taskTypes = taskTypeService.findTaskTypeAll(loginUser.getLoginUser().getCompanyId());
        Map<String, String> typeColorMap = buildTypeColorMap(taskTypes);

        model.addAttribute("projectId", pId);
        model.addAttribute("loginUserCode", userCode);
        model.addAttribute("project", projectService.findById(String.valueOf(pId)));
        model.addAttribute("sidebarMenu", "project");
        model.addAttribute("currentMenu", "kanban");
        model.addAttribute("statusList", statusList);
        model.addAttribute("tasksByStatus", tasksByStatus);
        model.addAttribute("taskPerms", taskPerms);
        model.addAttribute("isAdminOrOwner", isAdminOrOwner);
        model.addAttribute("typeColorMap", typeColorMap);

        return "weple/task/kanban";
    }

    // 카드 1개 즉시 이동 (기존 호환용으로 남겨둠). 관리자/소유자 여부를 반드시 함께 넘겨야 함 - 이전에는 이게 빠져 있어서
    // 관리자가 상태를 옮겨도 서버에서 항상 일반 담당자 권한으로만 체크되는 버그가 있었음.
    @PostMapping("/api/task/kanban/status")
    @ResponseBody
    public ResponseEntity<String> updateTaskKanbanStatus(
            @RequestParam("taskId") String taskId,
            @RequestParam("taskStatus") String taskStatus,
            @RequestParam("projectId") Long pId,
            @AuthenticationPrincipal LoginUserDetails loginUser) {

        if (loginUser == null || loginUser.getLoginUser() == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        boolean isAdminOrOwner = isAdminOrOwner(loginUser);

        try {
            taskKanbanService.updateTaskStatus(taskId, taskStatus, loginUser.getLoginUser().getUserCode(), pId, isAdminOrOwner);
            return ResponseEntity.ok("ok");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("상태 변경 중 오류가 발생했습니다.");
        }
    }

    // 요구사항: 초기화/완료 버튼으로 여러 건을 한 번에 저장 (드래그는 화면에서만 반영되고, '완료'를 눌러야 실제 저장됨)
    @PostMapping("/api/task/kanban/status/batch")
    @ResponseBody
    public ResponseEntity<String> updateTaskKanbanStatusBatch(
            @RequestBody TaskKanbanBatchRequest request,
            @AuthenticationPrincipal LoginUserDetails loginUser) {

        if (loginUser == null || loginUser.getLoginUser() == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        if (request == null || request.getChanges() == null || request.getChanges().isEmpty()) {
            return ResponseEntity.badRequest().body("변경할 항목이 없습니다.");
        }

        boolean isAdminOrOwner = isAdminOrOwner(loginUser);
        String userCode = loginUser.getLoginUser().getUserCode();

        try {
            for (TaskKanbanBatchRequest.Item item : request.getChanges()) {
                taskKanbanService.updateTaskStatus(item.getTaskId(), item.getTaskStatus(), userCode, request.getProjectId(), isAdminOrOwner);
            }
            return ResponseEntity.ok("ok");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("상태 변경 중 오류가 발생했습니다.");
        }
    }

    private boolean isAdminOrOwner(LoginUserDetails loginUser) {
        Integer ownerYn = loginUser.getLoginUser().getOwnerYn();
        Integer adminYn = loginUser.getLoginUser().getAdminYn();
        return (ownerYn != null && ownerYn == 1) || (adminYn != null && adminYn == 1);
    }
}