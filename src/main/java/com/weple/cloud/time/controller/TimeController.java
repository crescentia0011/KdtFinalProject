package com.weple.cloud.time.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
import com.weple.cloud.auth.service.LoginUserDetails;
import com.weple.cloud.history.task.service.TaskHistoryService;
import com.weple.cloud.project.service.ProjectService;
import com.weple.cloud.project.service.ProjectVO;
import com.weple.cloud.system.service.CodeValueService;
import com.weple.cloud.system.service.CodeValueVO;
import com.weple.cloud.task.service.TaskService;
import com.weple.cloud.task.service.TaskVO;
import com.weple.cloud.time.service.ProjectTimeSettingService;
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
	private final ProjectTimeSettingService projectTimeSettingService;
	private final TaskHistoryService taskHistoryService;

	private boolean isCompanyManager(com.weple.cloud.auth.service.LoginUserVO user) {
		return Integer.valueOf(1).equals(user.getOwnerYn())
			|| Integer.valueOf(1).equals(user.getAdminYn());
	}

	// -------------------------------프로젝트 내 소요시간------------------------------
	// 전체조회 (관리자 포함 누구나 본인이 등록한 건만 - 프로젝트 전체 현황은 전체 소요시간 페이지에서 확인)
	@GetMapping("/projectTimeList")
	public String projectTimeList(@RequestParam("projectId") long projectId,
			@RequestParam(value = "taskId", required = false) String taskId,
			@ModelAttribute("toastMessage") String toastMessage,
			@ModelAttribute("toastError") String toastError,
			Model model,
			@AuthenticationPrincipal LoginUserDetails loginUser) {

		// 멤버십 체크 (관리자는 항상 접근 가능)
		boolean isManager = isCompanyManager(loginUser.getLoginUser());
		try {
			if (!isManager && !projectService.isMember(loginUser.getLoginUser().getUserCode(), projectId)) {
				return "weple/access-denide";
			}
		} catch (Exception e) {
			return "weple/access-denide";
		}

		List<WorkTimeVO> list = timeService.findProjectTimeAll(projectId, loginUser.getLoginUser().getUserCode());
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
		model.addAttribute("isManager", isManager);
		model.addAttribute("loginUserCode", loginUser.getLoginUser().getUserCode());
		model.addAttribute("sidebarMenu", "project");
		model.addAttribute("project", projectService.findById(String.valueOf(projectId)));
		model.addAttribute("projectId", projectId);
		model.addAttribute("currentMenu", "time");
		model.addAttribute("moduleNames", projectService.findModuleNames(projectId));
		return "weple/time/project-total";
	}

	// 등록 폼
	@GetMapping("/insertProjectTime")
	public String insertProjectTimeForm(@RequestParam(value="projectId", required=false) Long projectId,
										@RequestParam(value="taskId", required=false) String taskId, Model model,
										@AuthenticationPrincipal LoginUserDetails loginUser) {

		boolean isManager = isCompanyManager(loginUser.getLoginUser());
		try {
			if (projectId == null || projectId == 0) {
				if (!isManager) return "weple/access-denide";
			} else if (!isManager && !projectService.isMember(loginUser.getLoginUser().getUserCode(), projectId)) {
				return "weple/access-denide";
			}
		} catch (Exception e) {
			return "weple/access-denide";
		}

		List<ProjectVO> projectList = projectService.findAll("");

	    ProjectVO currentProject = (projectId != null) ? projectService.findById(String.valueOf(projectId)) : null;
	    if (currentProject == null) {
	        currentProject = new ProjectVO();
	        currentProject.setProjectTitle("프로젝트 정보를 찾을 수 없습니다.");
	    }
	    model.addAttribute("currentProject", currentProject);

		List<TaskVO> taskList = (projectId != null) ? taskService.findAll(projectId) : new java.util.ArrayList<>();
		if (!isManager) {
			taskList = taskList.stream()
					.filter(t -> loginUser.getLoginUser().getUserCode().equals(t.getTaskManagerId()))
					.collect(Collectors.toList());
		}
		model.addAttribute("taskList", taskList);

		List<UserVO> userList = (projectId == null || projectId == 0)
		        ? userService.findAllActiveUsers()
		        : userService.findUsersByProjectId(String.valueOf(projectId));
		model.addAttribute("userList", userList);

	    WorkTimeVO workTimeVO = new WorkTimeVO();
	    if (projectId != null) {
	        workTimeVO.setProjectId(projectId);
	    }

	    List<CodeValueVO> workTypeList = codeValueService.findCodeValueAll(loginUser.getLoginUser().getCompanyId());
	    workTypeList = workTypeList.stream()
	    		.filter(vo -> vo.getWorkName() != null && !vo.getWorkName().isEmpty())
	    		.filter(vo -> "Y".equals(vo.getUsingYn()))
	    		.collect(Collectors.toList());
	    workTypeList = filterByProjectTimeSetting(workTypeList, projectId);

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
	    model.addAttribute("workTypeList", workTypeList);
	    model.addAttribute("loginUserCode", loginUser.getLoginUser().getUserCode());
	    model.addAttribute("loginUserName", loginUser.getLoginUser().getUserName());
		return "weple/time/insert";
	}

	// 등록 처리
	@PostMapping("/insertProjectTime")
	public String insertProjectTimeProcess(WorkTimeVO workTimeVO,
										   @RequestParam(value="taskId", required=false) String taskId,
										   @RequestParam(value="returnTo", required=false) String returnTo,
										   @AuthenticationPrincipal LoginUserDetails loginUser,
										   RedirectAttributes redirectAttributes) {
	    boolean isManager = isCompanyManager(loginUser.getLoginUser());
	    

	    
	    
	    Long projectId = workTimeVO.getProjectId();
	    try {
	        if (projectId == null || projectId == 0) {
	            if (!isManager) return "weple/access-denide";
	        } else if (!isManager && !projectService.isMember(loginUser.getLoginUser().getUserCode(), projectId)) {
	            return "weple/access-denide";
	        }
	        if (!isManager) {
	            TaskVO task = (workTimeVO.getTaskId() != null) ? taskService.findTaskDetail(workTimeVO.getTaskId()) : null;
	            if (task == null || !loginUser.getLoginUser().getUserCode().equals(task.getTaskManagerId())) {
	                return "weple/access-denide";
	            }
	        }
	    } catch (Exception e) {
	        return "weple/access-denide";
	    }
	    
	    TaskVO before = (taskId != null) ? taskService.findTaskDetail(taskId) : null;
	    long oldSpentHours = (before != null) ? before.getSpentHoursSum() : 0;

	    try {
	        timeService.addProjectTime(workTimeVO);
	        TaskVO after = (taskId != null) ? taskService.findTaskDetail(taskId) : null;
	        long newSpentHours = (after != null) ? after.getSpentHoursSum() : 0;
	        if (taskId != null && oldSpentHours != newSpentHours) {
	            taskHistoryService.insertHistory(
	                taskId, 
	                loginUser.getLoginUser().getUserCode(), 
	                "UPDATE",
	                null, null, 
	                null, null,
	                null, null, 
	                null, null,
	                null, null,
	                null, null,
	                null, null,
	                null, null,
	                null, null,
	                null, null,
	                null, null,
	                String.valueOf(oldSpentHours), 
	                String.valueOf(newSpentHours),
	                null, null  // 파일 이력
	            );
	        }
	    } catch (IllegalStateException ex) {
	        redirectAttributes.addFlashAttribute("toastType", "error");
	        redirectAttributes.addFlashAttribute("toastMessage", ex.getMessage());
	        // 실패 시엔 재입력해야 하므로 등록 폼으로 되돌아감 (기존 동작 유지)
	        if ("detail".equals(returnTo) && taskId != null && !taskId.isEmpty()) {
	            return "redirect:/project/task/detail/" + taskId + "?projectId=" + workTimeVO.getProjectId();
	        }
	        if (workTimeVO.getProjectId() != null && workTimeVO.getProjectId() != 0) {
	            return "redirect:/insertProjectTime?projectId=" + workTimeVO.getProjectId();
	        }
	        return "redirect:/insertProjectTime";
	    }

	    redirectAttributes.addFlashAttribute("toastMessage", "소요시간이 등록되었습니다.");

	    // 일감 상세 페이지의 모달에서 등록한 경우, 해당 일감 상세 페이지로 되돌아감
	    if ("detail".equals(returnTo) && taskId != null && !taskId.isEmpty()) {
	        return "redirect:/project/task/detail/" + taskId + "?projectId=" + workTimeVO.getProjectId();
	    }

	    // ✅ 등록 성공: projectId 있으면 프로젝트 내 소요시간 목록, 없으면(관리자 전체 등록) 전체 소요시간 목록
	    if (workTimeVO.getProjectId() != null && workTimeVO.getProjectId() != 0) {
	        return "redirect:/projectTimeList?projectId=" + workTimeVO.getProjectId();
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

	@GetMapping("/hasChildTask")
	@ResponseBody
	public boolean hasChildTask(@RequestParam("taskId") String taskId) {
	    List<TaskVO> descendants = new java.util.ArrayList<>();
	    collectDescendants(taskId, descendants, new java.util.HashSet<>());

	    if (descendants.isEmpty()) {
	        return false;
	    }
	    boolean allCompleted = descendants.stream()
	            .allMatch(c -> c.getTaskProgress() != null && c.getTaskProgress() == 100L);
	    return !allCompleted;
	}

	private void collectDescendants(String taskId, List<TaskVO> acc, java.util.Set<String> visited) {
	    if (!visited.add(taskId)) {
	        return;
	    }
	    List<TaskVO> children = taskService.findChildTask(taskId);
	    if (children == null || children.isEmpty()) {
	        return;
	    }
	    for (TaskVO child : children) {
	        acc.add(child);
	        collectDescendants(child.getTaskId(), acc, visited);
	    }
	}

	// 수정 폼
	@GetMapping("/updateProjectTime")
	public String updateProjectTimeForm(@RequestParam("workId") long workId,
										@RequestParam(value="projectId", required=false) Long projectId, Model model,
										@AuthenticationPrincipal LoginUserDetails loginUser) {
		WorkTimeVO workTime = timeService.findProjectTimeOne(workId);
		if (workTime == null) return "redirect:/projectTimeList";

		boolean isManager = isCompanyManager(loginUser.getLoginUser());
		if (!isManager && !loginUser.getLoginUser().getUserCode().equals(workTime.getUserCode())) {
			return "weple/access-denide";
		}

		List<CodeValueVO> workTypeList = codeValueService.findCodeValueAll(loginUser.getLoginUser().getCompanyId()).stream()
			.filter(vo -> vo.getWorkName() != null && !vo.getWorkName().isEmpty())
			.filter(vo -> "Y".equals(vo.getUsingYn()))
			.collect(Collectors.toList());
		workTypeList = filterByProjectTimeSetting(workTypeList, workTime.getProjectId());

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
	public String updateProjectTimeProcess(WorkTimeVO workTimeVO, RedirectAttributes ra,
			@AuthenticationPrincipal LoginUserDetails loginUser) {
		WorkTimeVO original = timeService.findProjectTimeOne(workTimeVO.getWorkId());
		if (original == null) return "redirect:/projectTimeList";

		boolean isManager = isCompanyManager(loginUser.getLoginUser());
		if (!isManager && !loginUser.getLoginUser().getUserCode().equals(original.getUserCode())) {
			return "weple/access-denide";
		}

		timeService.modifyProjectTime(workTimeVO);
		ra.addFlashAttribute("toastMessage", "소요시간이 수정되었습니다.");

		// ✅ 수정 성공: projectId 있으면 프로젝트 내 소요시간 목록, 없으면 전체 소요시간 목록
		if (workTimeVO.getProjectId() != null && workTimeVO.getProjectId() != 0) {
			return "redirect:/projectTimeList?projectId=" + workTimeVO.getProjectId();
		}
		return "redirect:/totalTimeList";
	}

	// 삭제 (관리자만 가능)
	@GetMapping("/deleteProjectTime")
	public String deleteProjectTime(@RequestParam("projectId") long projectId, @RequestParam("workId") long workId,
			@AuthenticationPrincipal LoginUserDetails loginUser) {
		if (!isCompanyManager(loginUser.getLoginUser())) {
			return "weple/access-denide";
		}
		long result = timeService.removeProjectTime(workId);
		return "redirect:/projectTimeList?projectId=" + projectId;
	}

	private List<CodeValueVO> filterByProjectTimeSetting(List<CodeValueVO> workTypeList, Long projectId) {
		if (projectId == null) {
			return workTypeList;
		}
		List<String> selectedIds = projectTimeSettingService.findSelectedClassificationIds(projectId);
		if (selectedIds == null || selectedIds.isEmpty()) {
			return workTypeList;
		}
		return workTypeList.stream()
				.filter(vo -> selectedIds.contains(vo.getTaskClassificationId()))
				.collect(Collectors.toList());
	}
}