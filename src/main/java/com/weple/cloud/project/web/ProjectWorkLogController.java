package com.weple.cloud.project.web;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

    @GetMapping("/project/worklog")
    public String projectWorkLogList(
            @RequestParam String projectId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String userCode,
            @RequestParam(required = false) List<String> typeNames,
            Model model) {

        List<WorkLogVO> list =
                projectWorkLogService.findAll(
                        projectId,
                        startDate,
                        endDate,
                        userCode,
                        typeNames);
        
        ProjectVO project = projectService.findById(projectId);

        model.addAttribute("workLogList", list);
        model.addAttribute("project", project);

        model.addAttribute("projectId", projectId);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("userCode", userCode);
        model.addAttribute("typeNames", typeNames);
        
        model.addAttribute("sidebarMenu", "project");
        model.addAttribute("currentMenu", "worklog");

        return "weple/project/projectworklog";
    }
}
