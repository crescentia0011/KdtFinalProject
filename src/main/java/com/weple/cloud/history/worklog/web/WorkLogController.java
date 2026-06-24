package com.weple.cloud.history.worklog.web;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.weple.cloud.history.worklog.service.WorkLogService;
import com.weple.cloud.history.worklog.service.WorkLogVO;
import com.weple.cloud.project.service.ProjectService;
import com.weple.cloud.project.service.ProjectVO;

@Controller
public class WorkLogController {
	private final WorkLogService workLogService;
	private final ProjectService projectService;
	//private final UserService userService;
	
	@Autowired
	public WorkLogController(WorkLogService workLogService, ProjectService projectService) {
		this.workLogService = workLogService;
		this.projectService = projectService;
		//this.userService = userService;
	}
	
	// 작업내역 조회
	@GetMapping("/worklog")
	public String workLogList(
			@RequestParam(required = false) String projectId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String userCode,
            @RequestParam(required = false) List<String> typeNames,
            @RequestParam(value = "page", defaultValue = "1") int page,
			Model model) {
		
		 // 날짜 기본값: 최근 5일
		if (startDate == null || startDate.isEmpty() || endDate == null || endDate.isEmpty()) {
            LocalDate today = LocalDate.now();
            endDate = today.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
            startDate = today.minusDays(4).format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
        }

        int pageSize = 10;
        int offset = (page - 1) * pageSize;
		
        List<WorkLogVO> list = workLogService.findAll(
                projectId, startDate, endDate, userCode, typeNames, offset, pageSize);

        int totalCount = workLogService.countAll(
                projectId, startDate, endDate, userCode, typeNames);

        int totalPages = (int) Math.ceil((double) totalCount / pageSize);
		
		List<ProjectVO> projectList = projectService.findAll("");
		model.addAttribute("projects", projectList);
		
		model.addAttribute("workLogList", list);
		model.addAttribute("projectId", projectId);
		model.addAttribute("startDate", startDate);
		model.addAttribute("endDate", endDate);
		model.addAttribute("userCode", userCode);
		model.addAttribute("typeNames", typeNames);
		model.addAttribute("currentPage", page);      
		model.addAttribute("totalPages", totalPages); 
		
		model.addAttribute("sidebarMenu", "work-history");
		model.addAttribute("currentMenu", "none");
		
		return "weple/history/worklog";
	}
}
