package com.weple.cloud.time.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.weple.cloud.admin.service.UserService;
import com.weple.cloud.admin.service.UserVO;
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
	private final UserService userService;

	// -------------------------------프로젝트 내 소요시간------------------------------
	// 전체조회
	@GetMapping("/projectTimeList")
	public String projectTimeList(@RequestParam("projectId") long projectId,
			@RequestParam(value = "taskId", required = false) String taskId,
			@ModelAttribute("toastMessage") String toastMessage,
			@ModelAttribute("toastError") String toastError,
			Model model) {
		List<WorkTimeVO> list = timeService.findProjectTimeAll(projectId);
		// taskId가 있으면 해당 일감만 필터링
		if (taskId != null && !taskId.isEmpty()) {
		    list = list.stream()
		        .filter(w -> taskId.equals(w.getTaskId()))
		        .collect(Collectors.toList());
		    model.addAttribute("filterTaskId", taskId);
		}
		model.addAttribute("projectTimeList", list);
		if (!list.isEmpty()) {
		    model.addAttribute("countSpentHour", list.get(0).getCountSpentHour());
		    model.addAttribute("totalSpentHour", list.get(0).getTotalSpentHour());
		}
		model.addAttribute("sidebarMenu", "project");
		model.addAttribute("project", projectService.findById(String.valueOf(projectId)));  // ✅ 이렇게
		model.addAttribute("projectId", projectId);
		model.addAttribute("currentMenu", "time");
		model.addAttribute("moduleNames", projectService.findModuleNames(projectId));
		return "weple/time/project-total";
	}

	// 등록 폼
	@GetMapping("/insertProjectTime")
	public String insertProjectTimeForm(@RequestParam(value="projectId", required=false) Long projectId,
										@RequestParam(value="taskId", required=false) String taskId, Model model) {
		List<ProjectVO> projectList = projectService.findAll("");
		
	    // 프로젝트 조회, 만약 조회 결과가 없으면 빈 객체라도 생성
	    ProjectVO currentProject = (projectId != null) ? projectService.findById(String.valueOf(projectId)) : null;
	    if (currentProject == null) {
	        currentProject = new ProjectVO();
	        currentProject.setProjectTitle("프로젝트 정보를 찾을 수 없습니다.");
	    }
	    model.addAttribute("currentProject", currentProject);
		
		// 일감 목록 조회 (projectId 없으면 빈 리스트)
		List<TaskVO> taskList = (projectId != null) ? taskService.findAll(projectId) : new java.util.ArrayList<>();
		for(TaskVO t : taskList) {
		    System.out.println("데이터 확인 -> 제목: " + t.getTaskTitle() + ", 설명: " + t.getTaskDescribe());
		}
		model.addAttribute("taskList", taskList);
		
		//사용자 목록 조회 - projectId 없으면(관리자) 전체 사용자, 있으면 프로젝트 참여자만
		// projectId가 없거나 0이면(관리자 전체 등록) 전체 사용자, 아니면 프로젝트 참여자만
		List<UserVO> userList = (projectId == null || projectId == 0)
		        ? userService.findAllActiveUsers()
		        : userService.findUsersByProjectId(String.valueOf(projectId));
		model.addAttribute("userList", userList);
		
		// WorkTimeVO 생성 및 projectId 할당
	    WorkTimeVO workTimeVO = new WorkTimeVO();
	    if (projectId != null) {
	        workTimeVO.setProjectId(projectId);
	    }
	    
	    //작업분류에 있는 null 제외하고 데이터 불러옴
	    List<CodeValueVO> workTypeList = codeValueService.findCodeValueAll();
	    workTypeList = workTypeList.stream()
	    		.filter(vo -> vo.getWorkName() != null && !vo.getWorkName().isEmpty())
	    		.collect(Collectors.toList());
	    
	    //사이드바
	    List<String> moduleNames = projectService.findModuleNames(projectId);
	    model.addAttribute("moduleNames", moduleNames);
	    if (projectId != null) {
	        model.addAttribute("project", projectService.findById(String.valueOf(projectId)));
	    }
	    model.addAttribute("taskId", taskId);
	    model.addAttribute("workTimeVO", workTimeVO);
		model.addAttribute("currentMenu", "time");
		model.addAttribute("sidebarMenu", "project");
		model.addAttribute("projectId", projectId);
	    model.addAttribute("projectList", projectList);
	    model.addAttribute("taskList", taskList);
	    model.addAttribute("workTypeList", workTypeList);
		return "weple/time/insert";
	}

	// 등록 처리
	@PostMapping("/insertProjectTime")
	public String insertProjectTimeProcess(WorkTimeVO workTimeVO,
										   @RequestParam(value="taskId", required=false) String taskId,
										   RedirectAttributes redirectAttributes) {
	    timeService.addProjectTime(workTimeVO);
	    redirectAttributes.addFlashAttribute("toastMessage", "소요시간이 등록되었습니다.");
	    if (workTimeVO.getProjectId() != null && workTimeVO.getProjectId() != 0) {
	        String redirectUrl = "redirect:/projectTimeList?projectId=" + workTimeVO.getProjectId();
	        if (taskId != null && !taskId.isEmpty()) {
	            redirectUrl += "&taskId=" + taskId;  // ✅ taskId 있으면 추가
	        }
	        return redirectUrl;
	    }
	    return "redirect:/totalTimeList";
	}

	//일감 설명 가져옴
	@GetMapping("/getTaskDetail")
	@ResponseBody
	public TaskVO getTaskDetail(@RequestParam("taskId") String taskId) {
	    return taskService.findTaskDetail(taskId);
	}

	// 프로젝트별 일감 목록 (관리자 소요시간 등록 시 프로젝트 선택 후 Ajax 호출)
	@GetMapping("/getTasksByProject")
	@ResponseBody
	public List<TaskVO> getTasksByProject(@RequestParam("projectId") Long projectId) {
	    return taskService.findAll(projectId);
	}
	
	// 수정 폼
	@GetMapping("/updateProjectTime")
	public String updateProjectTimeForm(@RequestParam("workId") long workId,
										@RequestParam(value="projectId", required=false) Long projectId, Model model) {
		WorkTimeVO workTime = timeService.findProjectTimeOne(workId);
		if (workTime == null) return "redirect:/projectTimeList";

		// 작업분류 목록
		List<CodeValueVO> workTypeList = codeValueService.findCodeValueAll().stream()
			.filter(vo -> vo.getWorkName() != null && !vo.getWorkName().isEmpty())
			.collect(Collectors.toList());

		// 사용자 목록 (프로젝트 참여자)
		List<UserVO> userList = userService.findUsersByProjectId(String.valueOf(workTime.getProjectId()));

		model.addAttribute("workTime", workTime);
		model.addAttribute("workTypeList", workTypeList);
		model.addAttribute("userList", userList);
		model.addAttribute("currentMenu", "time");
		model.addAttribute("sidebarMenu", "project");
		model.addAttribute("project", projectService.findById(String.valueOf(workTime.getProjectId())));
		List<String> moduleNames = projectService.findModuleNames(workTime.getProjectId());
		model.addAttribute("moduleNames", moduleNames);
		return "weple/time/edit";
	}
	
	// 수정 처리
	@PostMapping("/updateProjectTime")
	public String updateProjectTimeProcess(WorkTimeVO workTimeVO, RedirectAttributes ra) {
		timeService.modifyProjectTime(workTimeVO);
		ra.addFlashAttribute("toastMessage", "소요시간이 수정되었습니다.");
		// projectId가 있으면 프로젝트 소요시간 목록으로, 없으면 전체 소요시간 목록으로
		if (workTimeVO.getProjectId() != null && workTimeVO.getProjectId() != 0) {
			return "redirect:/projectTimeList?projectId=" + workTimeVO.getProjectId();
		}
		return "redirect:/totalTimeList";
	}
	
	// 삭제
	@GetMapping("/deleteProjectTime")
	public String deleteProjectTime(@RequestParam("projectId") long projectId, @RequestParam("workId") long workId) {
		long result = timeService.removeProjectTime(workId);
		return "redirect:/projectTimeList?projectId=" + projectId;
	}
}