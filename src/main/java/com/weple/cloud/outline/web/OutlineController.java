package com.weple.cloud.outline.web;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.weple.cloud.outline.service.OutlineService;
import com.weple.cloud.outline.service.ProjectGroupMemberDTO;
import com.weple.cloud.outline.service.ProjectProgressDTO;
import com.weple.cloud.project.service.ProjectVO;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class OutlineController {
	
	private final OutlineService outlineService;
	
	// 프로젝트 개요 조회
	@GetMapping("/project/outline")
	public String getProjectOutline(@RequestParam Long projectId, Model model) {
		
		model.addAttribute("currentMenu", "outline");
		model.addAttribute("sidebarMenu", "project");
		model.addAttribute("projectId", projectId); 
		
		// 프로젝트 정보 조회
		ProjectVO project = outlineService.getProjectById(projectId);
	    model.addAttribute("project", project);
	    
		// 그룹별 프로젝트 참여 멤버 조회
		List<ProjectGroupMemberDTO> groupMembers = outlineService.selectProjectMembersByGroup(projectId);
		
		model.addAttribute("groupMembers", groupMembers);
		
		
		// 프로젝트 총 추정/소요 시간 조회 
		ProjectProgressDTO progressData = outlineService.getProjectProgress(projectId);
		model.addAttribute("progressData", progressData);
		
		return "weple/outline/outline";
	}
}
