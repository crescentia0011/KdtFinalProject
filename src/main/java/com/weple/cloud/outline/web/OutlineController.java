package com.weple.cloud.outline.web;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.weple.cloud.outline.service.OutlineService;
import com.weple.cloud.outline.service.ProjectGroupMemberDTO;
import com.weple.cloud.outline.service.ProjectProgressDTO;
import com.weple.cloud.project.service.ProjectService;
import com.weple.cloud.project.service.ProjectVO;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class OutlineController {
	
	private final OutlineService outlineService;
	private final ProjectService projectService;
	
	// 프로젝트 개요 조회
	@GetMapping("/project/outline")
	public String getProjectOutline(@RequestParam Long projectId, Model model) {
		
		model.addAttribute("currentMenu", "outline");
		model.addAttribute("sidebarMenu", "project");
		model.addAttribute("projectId", projectId); 
		model.addAttribute("project", projectService.findById(String.valueOf(projectId)));
		
		// 프로젝트 정보 조회 - 삭제되었거나 존재하지 않으면 안내 후 접근 제한
		ProjectVO project = outlineService.getProjectById(projectId);
		if (project == null) {
		    model.addAttribute("accessDenideTitle", "삭제되었거나 존재하지 않는 프로젝트입니다.");
		    model.addAttribute("accessDenideMessage", "요청하신 프로젝트를 찾을 수 없습니다. 삭제되었거나 잘못된 경로일 수 있습니다.");
		    return "weple/access-denide";
		}
	    
		// 그룹별 프로젝트 참여 멤버 조회
		List<ProjectGroupMemberDTO> groupMembers = outlineService.selectProjectMembersByGroup(projectId);
		
		model.addAttribute("groupMembers", groupMembers);
		
		
		// 프로젝트 총 추정/소요 시간 조회 
		ProjectProgressDTO progressData = outlineService.getProjectProgress(projectId);
		model.addAttribute("progressData", progressData);
		
		return "weple/outline/outline";
	}
}
