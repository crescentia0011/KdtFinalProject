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
import com.weple.cloud.project.service.ProjectService;
import com.weple.cloud.project.service.ProjectVO;
import com.weple.cloud.system.service.CodeValueService;
import com.weple.cloud.system.service.CodeValueVO;
import com.weple.cloud.task.service.TaskService;
import com.weple.cloud.task.service.TaskVO;
import com.weple.cloud.time.service.TimeService;
import com.weple.cloud.time.service.ProjectTimeSettingService;
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
		model.addAttribute("project", projectService.findById(String.valueOf(projectId)));  // ✅ 이렇게
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
				// 프로젝트 선택 없이(=전체 소요시간 페이지에서) 진입하는 등록은 관리자만 허용
				if (!isManager) return "weple/access-denide";
			} else if (!isManager && !projectService.isMember(loginUser.getLoginUser().getUserCode(), projectId)) {
				return "weple/access-denide";
			}
		} catch (Exception e) {
			return "weple/access-denide";
		}

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
		// 일반 사용자는 본인이 담당자로 지정된 일감에만 소요시간을 등록할 수 있음 (관리자는 일감 배정 대상이 아니라 전체 허용)
		if (!isManager) {
			taskList = taskList.stream()
					.filter(t -> loginUser.getLoginUser().getUserCode().equals(t.getTaskManagerId()))
					.collect(Collectors.toList());
		}
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
	    
	    //작업분류에 있는 null 제외하고, 사용중(Y)인 것만 불러옴
	    List<CodeValueVO> workTypeList = codeValueService.findCodeValueAll(loginUser.getLoginUser().getCompanyId());
	    workTypeList = workTypeList.stream()
	    		.filter(vo -> vo.getWorkName() != null && !vo.getWorkName().isEmpty())
	    		.filter(vo -> "Y".equals(vo.getUsingYn()))
	    		.collect(Collectors.toList());
	    // 설정 > 시간추적 탭에서 이 프로젝트가 사용 선택한 작업분류만 남김
	    workTypeList = filterByProjectTimeSetting(workTypeList, projectId);
	    
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
	        // 일반 사용자는 본인이 담당자로 지정된 일감에만 등록 가능 (관리자는 일감 배정 대상이 아니라 예외 없이 전체 허용)
	        if (!isManager) {
	            TaskVO task = (workTimeVO.getTaskId() != null) ? taskService.findTaskDetail(workTimeVO.getTaskId()) : null;
	            if (task == null || !loginUser.getLoginUser().getUserCode().equals(task.getTaskManagerId())) {
	                return "weple/access-denide";
	            }
	        }
	    } catch (Exception e) {
	        return "weple/access-denide";
	    }

	    try {
	        timeService.addProjectTime(workTimeVO);
	    } catch (IllegalStateException ex) {
	        redirectAttributes.addFlashAttribute("toastType", "error");
	        redirectAttributes.addFlashAttribute("toastMessage", ex.getMessage());
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
	    // 그 외(프로젝트 소요시간 탭 / 전체 소요시간 목록에서 진입한 경우)는
	    // 목록이 아니라 일감을 선택하던 등록 페이지 자체로 되돌아감
	    if (workTimeVO.getProjectId() != null && workTimeVO.getProjectId() != 0) {
	        return "redirect:/insertProjectTime?projectId=" + workTimeVO.getProjectId();
	    }
	    return "redirect:/insertProjectTime";
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

	// 진척도 수정 가능 여부 판단용
	// 규칙: ① 하위일감이 없으면 잠금 아님(false)
	//      ② 하위/하위의 하위일감(전체 계층) 중 진행률 100%가 아닌 것이 하나라도 있으면 잠금(true)
	//      ③ 하위 계층이 있어도 전부 100%(완료)면 잠금 아님(false)
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

	// taskId 하위의 모든 일감(자식, 손자, ...)을 재귀적으로 수집
	// visited: 데이터가 잘못 얽혀 순환 참조가 생기는 경우를 대비한 방어 로직
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

		// 본인이 등록한 건이거나 관리자만 수정 가능
		boolean isManager = isCompanyManager(loginUser.getLoginUser());
		if (!isManager && !loginUser.getLoginUser().getUserCode().equals(workTime.getUserCode())) {
			return "weple/access-denide";
		}

		// 작업분류 목록 (사용중인 것 중, 이 프로젝트가 사용 선택한 것만)
		List<CodeValueVO> workTypeList = codeValueService.findCodeValueAll(loginUser.getLoginUser().getCompanyId()).stream()
			.filter(vo -> vo.getWorkName() != null && !vo.getWorkName().isEmpty())
			.filter(vo -> "Y".equals(vo.getUsingYn()))
			.collect(Collectors.toList());
		workTypeList = filterByProjectTimeSetting(workTypeList, workTime.getProjectId());

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
		// projectId가 있으면 프로젝트 소요시간 목록으로, 없으면 전체 소요시간 목록으로
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

	// 설정 > 시간추적 탭에서 프로젝트가 사용 선택한 작업분류만 남김
	// projectId가 없거나(관리자 전체 등록), 프로젝트가 아직 사용 선택을 한 번도 안 했으면 원본 목록 그대로 둠
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