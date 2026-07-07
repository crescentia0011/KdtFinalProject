package com.weple.cloud.project.web;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.weple.cloud.admin.service.UserService;
import com.weple.cloud.admin.service.UserVO;
import com.weple.cloud.auth.service.LoginUserDetails;
import com.weple.cloud.history.worklog.service.WorkLogVO;
import com.weple.cloud.project.service.ProjectService;
import com.weple.cloud.project.service.ProjectVO;
import com.weple.cloud.project.service.ProjectWorkLogService;
import com.weple.cloud.system.service.TaskTypeService;
import com.weple.cloud.system.service.TaskTypeVO;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ProjectWorkLogController {
    private final ProjectWorkLogService projectWorkLogService;
    private final ProjectService projectService;
    private final UserService userService;
    private final TaskTypeService taskTypeService;

    // 뱃지 색상 순환용 (고정 5색을 순서대로 순환 적용)
    private static final String[] TYPE_COLOR_CLASSES = {
            "type-color-1", "type-color-2", "type-color-3", "type-color-4"
    };

    private boolean isCompanyManager(com.weple.cloud.auth.service.LoginUserVO user) {
        return Integer.valueOf(1).equals(user.getOwnerYn())
            || Integer.valueOf(1).equals(user.getAdminYn());
    }

    // 일감유형 이름 -> 뱃지 색상 클래스 매핑 (task_types position 순서 기준 4색 순환)
    private Map<String, String> buildTypeColorMap(List<TaskTypeVO> taskTypes) {
        Map<String, String> colorMap = new LinkedHashMap<>();
        for (int i = 0; i < taskTypes.size(); i++) {
            colorMap.put(taskTypes.get(i).getTypeName(), TYPE_COLOR_CLASSES[i % TYPE_COLOR_CLASSES.length]);
        }
        return colorMap;
    }

    // 프로젝트 내 작업내역 조회
    @GetMapping("/project/worklog")
    public String projectWorkLogList(
            @AuthenticationPrincipal LoginUserDetails loginUser,
            @RequestParam String projectId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String userCode,
            @RequestParam(required = false) List<String> typeNames,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "search", required = false) String search,
            Model model) {

        // 멤버십 체크
        try {
            if (!isCompanyManager(loginUser.getLoginUser())
                    && !projectService.isMember(
                            loginUser.getLoginUser().getUserCode(),
                            Long.parseLong(projectId))) {
                return "weple/access-denide";
            }
        } catch (Exception e) {
            return "weple/access-denide";
        }

        // 날짜 기본값: 최근 5일
        if (startDate == null || startDate.isBlank()) {
            LocalDate today = LocalDate.now();

            StringBuilder redirectUrl = new StringBuilder("redirect:/project/worklog");
            redirectUrl.append("?projectId=").append(projectId);
            redirectUrl.append("&startDate=").append(today.minusDays(4).format(DateTimeFormatter.ofPattern("yyyy.MM.dd")));
            redirectUrl.append("&endDate=").append(today.format(DateTimeFormatter.ofPattern("yyyy.MM.dd")));

            if (userCode != null && !userCode.isBlank())
                redirectUrl.append("&userCode=").append(userCode);
            if (typeNames != null)
                for (String t : typeNames) redirectUrl.append("&typeNames=").append(t);

            return redirectUrl.toString();
        }

        List<WorkLogVO> list = null;
        int totalPages = 0;
        String targetDate = null;
        Double totalSpentHour = null;

        // 기본 기간(최근 5일)으로 들어와도 바로 조회되어야 하므로 search 파라미터로 게이트하지 않음.
        // "검색" 버튼은 필터(담당자/유형) 값을 조회 조건에 반영하는 용도로만 사용한다.
        List<String> allDates = projectWorkLogService.findDistinctDates(
                projectId, startDate, endDate, userCode, typeNames);

        totalPages = allDates.size();

        if (!allDates.isEmpty() && page <= allDates.size()) {
            targetDate = allDates.get(page - 1);
            list = projectWorkLogService.findByDate(
                    targetDate, projectId, userCode, typeNames);
        }

        totalSpentHour = projectWorkLogService.sumSpentHour(
                projectId, startDate, endDate, userCode, typeNames);

        ProjectVO project = projectService.findById(projectId);
        List<String> moduleNames = projectService.findActiveModuleNames(Long.parseLong(projectId));

        List<UserVO> userList = userService.findUsersByProjectId(projectId);
        model.addAttribute("users", userList);

        // 일감유형 (필터 체크박스 + 뱃지 색상용, task_types 테이블에서 동적으로 가져옴)
        List<TaskTypeVO> taskTypes = taskTypeService.findTaskTypeAll(loginUser.getLoginUser().getCompanyId());
        Map<String, String> typeColorMap = buildTypeColorMap(taskTypes);

        model.addAttribute("workLogList",    list);
        model.addAttribute("targetDate",     targetDate);
        model.addAttribute("searched",       true);
        model.addAttribute("project",        project);
        model.addAttribute("moduleNames",    moduleNames);
        model.addAttribute("projectId",      projectId);
        model.addAttribute("startDate",      startDate);
        model.addAttribute("endDate",        endDate);
        model.addAttribute("userCode",       userCode);
        model.addAttribute("typeNames",      typeNames);
        model.addAttribute("currentPage",    page);
        model.addAttribute("totalPages",     totalPages);
        model.addAttribute("totalSpentHour", totalSpentHour);
        model.addAttribute("taskTypes",      taskTypes);
        model.addAttribute("typeColorMap",   typeColorMap);

        model.addAttribute("sidebarMenu", "project");
        model.addAttribute("currentMenu", "worklog");

        return "weple/project/projectworklog";
    }
}