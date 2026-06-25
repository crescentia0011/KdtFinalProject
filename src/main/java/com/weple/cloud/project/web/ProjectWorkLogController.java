package com.weple.cloud.project.web;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.weple.cloud.admin.service.UserService;
import com.weple.cloud.admin.service.UserVO;
import com.weple.cloud.history.worklog.service.WorkLogVO;
import com.weple.cloud.project.service.ProjectService;
import com.weple.cloud.project.service.ProjectVO;
import com.weple.cloud.project.service.ProjectWorkLogService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ProjectWorkLogController {
	private final ProjectWorkLogService projectWorkLogService;
	private final ProjectService projectService;
	private final UserService userService;

    @GetMapping("/project/worklog")
    public String projectWorkLogList(
            @RequestParam String projectId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String userCode,
            @RequestParam(required = false) List<String> typeNames,
            @RequestParam(value = "page", defaultValue = "1") int page,
            Model model) {
    	
    	if (startDate == null || startDate.isBlank()) {
    	    LocalDate today = LocalDate.now();
    	    
    	    StringBuilder redirectUrl = new StringBuilder("redirect:/project/worklog");
    	    redirectUrl.append("?projectId=").append(projectId);
    	    redirectUrl.append("&startDate=").append(today.minusDays(4).format(DateTimeFormatter.ofPattern("yyyy.MM.dd")));
    	    redirectUrl.append("&endDate=").append(today.format(DateTimeFormatter.ofPattern("yyyy.MM.dd")));
    	    
    	    if (userCode != null && !userCode.isBlank()) {
    	        redirectUrl.append("&userCode=").append(userCode);
    	    }
    	    if (typeNames != null) {
    	        for (String t : typeNames) {
    	            redirectUrl.append("&typeNames=").append(t);
    	        }
    	    }
    	    return redirectUrl.toString();
    	}
        
        int pageSize = 10;
        int offset = (page - 1) * pageSize;

        List<WorkLogVO> list = projectWorkLogService.findAll(
                projectId, startDate, endDate, userCode, typeNames, offset, pageSize);

        int totalCount = projectWorkLogService.countAll(
                projectId, startDate, endDate, userCode, typeNames);

        int totalPages = (int) Math.ceil((double) totalCount / pageSize);
        
        ProjectVO project = projectService.findById(projectId);
        List<String> moduleNames = projectService.findModuleNames(Long.parseLong(projectId));
        
        // 프로젝트 구성원 사용자 목록
        List<UserVO> userList = userService.findUsersByProjectId(projectId);
        model.addAttribute("users", userList);

        model.addAttribute("workLogList", list);
        model.addAttribute("project", project);
        model.addAttribute("moduleNames", moduleNames);
        model.addAttribute("projectId", projectId);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("userCode", userCode);
        model.addAttribute("typeNames", typeNames);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        
        
        model.addAttribute("sidebarMenu", "project");
        model.addAttribute("currentMenu", "worklog");

        return "weple/project/projectworklog";
    }
}
