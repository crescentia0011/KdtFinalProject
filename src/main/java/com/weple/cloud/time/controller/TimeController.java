package com.weple.cloud.time.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.weple.cloud.project.service.ProjectService;
import com.weple.cloud.project.service.ProjectVO;
import com.weple.cloud.system.service.CodeValueService;
import com.weple.cloud.system.service.CodeValueVO;
import com.weple.cloud.task.service.TaskService;
import com.weple.cloud.task.service.TaskVO;
import com.weple.cloud.time.service.TimeService;
import com.weple.cloud.time.service.WorkTimeVO;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class TimeController {

	private final TaskService taskService;
	private final ProjectService projectService;
	private final CodeValueService codeValueService;
	private final TimeService timeService;

	// -------------------------------프로젝트 내 소요시간------------------------------
	// 전체조회
	@GetMapping("/projectTimeList")
	public String projectTimeList(@RequestParam("projectId") long projectId, Model model) {
		List<WorkTimeVO> list = timeService.findProjectTimeAll(projectId);
		model.addAttribute("projectTimeList", list);
		if (!list.isEmpty()) {
		    model.addAttribute("countSpentHour", list.get(0).getCountSpentHour());
		    model.addAttribute("totalSpentHour", list.get(0).getTotalSpentHour());
		}
		model.addAttribute("sidebarMenu", "project");
		model.addAttribute("projectId", projectId);
		model.addAttribute("currentMenu", "time");
		model.addAttribute("moduleNames", projectService.findModuleNames(projectId));
		return "weple/time/project-total";
	}

	// 등록 폼
	@GetMapping("/insertProjectTime")
	public String insertProjectTimeForm(@RequestParam(value="projectId", required=false) Long projectId, Model model) {
		List<ProjectVO> projectList = projectService.findAll("");
		if (projectId == null && projectList != null && !projectList.isEmpty()) {
	        projectId = projectList.get(0).getProjectId();
	    }
		
		model.addAttribute("currentMenu", "time");
		model.addAttribute("sidebarMenu", "project");
		model.addAttribute("projectId", projectId);
		
		List<String> moduleNames = projectService.findModuleNames(projectId);
	    model.addAttribute("moduleNames", moduleNames);
		    
		List<TaskVO> taskList = new ArrayList<>();
	    if (projectId != null) {
	        taskList = taskService.findAll(projectId);
	    }
	    
	  //작업분류에 있는 null 제외하고 데이터 불러옴
	    List<CodeValueVO> workTypeList = codeValueService.findCodeValueAll();
	    workTypeList = workTypeList.stream()
	    	    .filter(vo -> vo.getWorkName() != null && !vo.getWorkName().isEmpty())
	    	    .collect(Collectors.toList());
	    
	    model.addAttribute("projectList", projectList);
	    model.addAttribute("taskList", taskList);
	    model.addAttribute("workTypeList", workTypeList);
	    model.addAttribute("workTimeVO", new WorkTimeVO()); // 입력할 객체 초기화
		return "weple/time/insert";
	}

	// 등록 처리
	@PostMapping("/insertProjectTime")
	public String insertProjectTimeProcess(WorkTimeVO workTimeVO, RedirectAttributes redirectAttributes) {
	    timeService.addProjectTime(workTimeVO);
	    redirectAttributes.addAttribute("projectId", workTimeVO.getProjectId());
	    return "redirect:/projectTimeList";
	}

	// 수정 폼
	@GetMapping("/updateProjectTime")
	public String updateProjectTimeForm(@RequestParam("workId") long workId, Model model) {
		return "weple/time/insert";
	}
	
	// 수정 처리
	@PostMapping("/updateProjectTime")
	public String updateProjectTimeProcess(WorkTimeVO workTimeVO) {
		timeService.modifyProjectTime(workTimeVO);
		return "redirect:/projectTimeList";
	}
	
	// 삭제
	@GetMapping("/deleteProjectTime")
	public String deleteProjectTime(@RequestParam("projectId") long projectId, @RequestParam("workId") long workId) {
		long result = timeService.removeProjectTime(workId);
		return "redirect:/projectTimeList?projectId=" + projectId;
	}
}
