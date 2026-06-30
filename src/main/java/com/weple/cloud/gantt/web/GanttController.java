package com.weple.cloud.gantt.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.weple.cloud.project.service.ProjectService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/project/gantt")
public class GanttController {

    private final ProjectService projectService;

    // 간트차트 화면 조회
    @GetMapping
    public String ganttChart(@RequestParam Long projectId, Model model) {
        
        // 기존 툴바/사이드바 UI 유지를 위한 속성 세팅
        model.addAttribute("currentMenu", "gantt");
        model.addAttribute("sidebarMenu", "project");
        model.addAttribute("projectId", projectId);
        model.addAttribute("project", projectService.findById(String.valueOf(projectId)));
        
        // 알맞은 뷰 경로 리턴
        return "weple/gantt/chart"; 
    }
}
