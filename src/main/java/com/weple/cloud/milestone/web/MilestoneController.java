package com.weple.cloud.milestone.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.weple.cloud.auth.service.LoginUserDetails;
import com.weple.cloud.auth.service.LoginUserVO;
import com.weple.cloud.milestone.service.MilestoneDetailVO;
import com.weple.cloud.milestone.service.MilestoneInfoVO;
import com.weple.cloud.milestone.service.MilestoneService;
import com.weple.cloud.milestone.service.MilestoneVO;
import com.weple.cloud.project.service.ProjectService;
import com.weple.cloud.project.service.ProjectVO;
import com.weple.cloud.repository.service.RepositoryService; // [추가] 권한 조회를 위해 주입
import com.weple.cloud.system.service.TaskTypeVO;
import com.weple.cloud.task.service.TaskVO;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/project/milestone") 
public class MilestoneController {

	private static final String PERMISSION_MILESTONE_CREATE_UPDATE_DELETE = "k1_version";
	private static final String PERMISSION_TASK_CREATE = "k3_add";
	private static final String PERMISSION_TASK_UPDATE = "k3_edit";
	private static final String PERMISSION_TASK_MYUPDATE = "k3_myedit";
	
	private final MilestoneService milestoneService;
	private final ProjectService projectService;
	private final RepositoryService repositoryService; // [추가] 팀원의 프로젝트 권한 조회 메서드 활용

	// 로드맵 전체 조회
	@GetMapping
	public String milestoneList(@AuthenticationPrincipal LoginUserDetails loginUser, // [추가]
								@RequestParam Long projectId, 
								Model model) {
		// 권한 체크 및 뷰단 버튼 제어용 속성 추가
		Set<String> permissionCodes = findMilestonePermissionCodes(loginUser, projectId);
		addMilestonePermissionAttributes(model, permissionCodes);

		List<MilestoneInfoVO> list = milestoneService.getRoadmapList(projectId); 
		
		model.addAttribute("currentMenu", "milestone");
		model.addAttribute("sidebarMenu", "project");
		model.addAttribute("projectId", projectId);
		model.addAttribute("project", projectService.findById(String.valueOf(projectId)));
		model.addAttribute("milestones", list);
		
		return "weple/milestone/list"; 
	}
	
	// 마일스톤 상세조회
	@GetMapping("/detail")
    public String getMilestoneDetail(@AuthenticationPrincipal LoginUserDetails loginUser, // [추가]
    								 @RequestParam Long projectId,
    								 @RequestParam Long milestoneId,
                                     @RequestParam(value = "page", defaultValue = "1") int page,
                                     Model model) {
		// 권한 체크 및 뷰단 버튼 제어용 속성 추가
		Set<String> permissionCodes = findMilestonePermissionCodes(loginUser, projectId);
		addMilestonePermissionAttributes(model, permissionCodes);
		
		// [추가] 일감 추가 권한 및 일감 편집(전체 편집 OR 내 일감 편집) 권한을 화면단에 넘겨줌
		model.addAttribute("canAddTask", hasMilestonePermission(permissionCodes, PERMISSION_TASK_CREATE));
		model.addAttribute("canEditTask", hasMilestonePermission(permissionCodes, PERMISSION_TASK_UPDATE)
				|| hasMilestonePermission(permissionCodes, PERMISSION_TASK_MYUPDATE));

        // 1. 마일스톤 상세 정보 및 4대 분류 통계 통합 조회
        MilestoneDetailVO detailInfo = milestoneService.getMilestoneDetailInfo(projectId, milestoneId);
        
        // 2. 연결된 일감 리스트 페이징 조회 (한 페이지 최대 20개, 최신순 정렬)
        int pageSize = 20;
        List<TaskVO> paginatedTasks = milestoneService.getMilestoneTasksWithPaging(projectId, milestoneId, page, pageSize);
        
        int totalTaskCount = detailInfo.getTotalTaskCount();

        model.addAttribute("projectId", projectId); 
        model.addAttribute("detail", detailInfo);
        model.addAttribute("taskList", paginatedTasks);
        model.addAttribute("currentPage", page);
        model.addAttribute("currentMenu", "milestone");
        model.addAttribute("sidebarMenu", "project");
        model.addAttribute("project", projectService.findById(String.valueOf(projectId)));
        model.addAttribute("totalTasks", totalTaskCount);
        model.addAttribute("totalPages", (int) Math.ceil((double) totalTaskCount / pageSize));
        
        List<TaskTypeVO> taskTypeList = milestoneService.getTaskTypeList();
        model.addAttribute("taskTypeList", taskTypeList);

        return "weple/milestone/detail"; 
    }
	
	// 버전 등록 페이지 조회
	@GetMapping("/version/insert")
	public String versionInsertForm(@AuthenticationPrincipal LoginUserDetails loginUser, // [추가]
									@RequestParam Long projectId, 
									Model model) {
		// 권한 체크 수행
		Set<String> permissionCodes = findMilestonePermissionCodes(loginUser, projectId);
		if (!hasMilestonePermission(permissionCodes, PERMISSION_MILESTONE_CREATE_UPDATE_DELETE)) {
			return "weple/access-denide";
		}
		addMilestonePermissionAttributes(model, permissionCodes);

	    model.addAttribute("currentMenu", "milestone");
	    model.addAttribute("sidebarMenu", "project");
	    model.addAttribute("projectId", projectId);
	    model.addAttribute("project", projectService.findById(String.valueOf(projectId)));
	    
	    ProjectVO project = milestoneService.findById(projectId); 
	    
	    model.addAttribute("projectId", projectId);
	    model.addAttribute("projectStart", project.getCreatedAt()); 
	    model.addAttribute("projectFinish", project.getFinishDate());
	    
	    return "weple/milestone/versionRegister";
	}

	// 버전 등록하기 
	@PostMapping("/version/insert")
	public String versionInsert(
	        @RequestParam Long projectId,
	        @AuthenticationPrincipal LoginUserDetails loginUser, 
	        MilestoneVO milestoneVO) {
	    
		// 권한 체크 수행
		Set<String> permissionCodes = findMilestonePermissionCodes(loginUser, projectId);
		if (!hasMilestonePermission(permissionCodes, PERMISSION_MILESTONE_CREATE_UPDATE_DELETE)) {
			return "weple/access-denide";
		}

	    String userCode = loginUser.getLoginUser().getUserCode();
	    milestoneVO.setUserCode(userCode);
	    milestoneVO.setProjectId(projectId);
	    
	    if (milestoneVO.getMilestoneStatus() == null) {
	        milestoneVO.setMilestoneStatus("g1"); 
	    }
	    
	    milestoneService.addVersion(milestoneVO);
	    		    
	    return "redirect:/project/milestone?projectId=" + projectId;
	}

	// 마일스톤 등록 페이지 조회
	@GetMapping("/insert")
	public String milestoneInsertForm(@AuthenticationPrincipal LoginUserDetails loginUser, // [추가]
									  @RequestParam Long projectId, 
									  Model model) {
		// 권한 체크 수행
		Set<String> permissionCodes = findMilestonePermissionCodes(loginUser, projectId);
		if (!hasMilestonePermission(permissionCodes, PERMISSION_MILESTONE_CREATE_UPDATE_DELETE)) {
			return "weple/access-denide";
		}
		addMilestonePermissionAttributes(model, permissionCodes);

	    model.addAttribute("currentMenu", "milestone");
	    model.addAttribute("sidebarMenu", "project");
	    model.addAttribute("projectId", projectId);
	    model.addAttribute("project", projectService.findById(String.valueOf(projectId)));
	    
	    List<TaskTypeVO> taskTypeList = milestoneService.getTaskTypeList();
	    model.addAttribute("taskTypeList", taskTypeList);
	    
	    List<MilestoneVO> parentMilestoneList = milestoneService.getMilestoneListByProjectId(projectId);
	    model.addAttribute("parentMilestoneList", parentMilestoneList);
	    
	    ProjectVO project = milestoneService.findById(projectId); 
	    
	    model.addAttribute("projectId", projectId);
	    model.addAttribute("projectStart", project.getCreatedAt()); 
	    model.addAttribute("projectFinish", project.getFinishDate());
	    
	    return "weple/milestone/register";
	}

	// 마일스톤 등록하기 
	@PostMapping("/insert")
	public String milestoneInsert(
	        @RequestParam Long projectId,
	        @RequestParam(value = "taskIds", required = false) List<String> taskIds, 
	        @AuthenticationPrincipal LoginUserDetails loginUser, 
	        MilestoneVO milestoneVO) {
	    
		// 권한 체크 수행
		Set<String> permissionCodes = findMilestonePermissionCodes(loginUser, projectId);
		if (!hasMilestonePermission(permissionCodes, PERMISSION_MILESTONE_CREATE_UPDATE_DELETE)) {
			return "weple/access-denide";
		}

	    String userCode = loginUser.getLoginUser().getUserCode();
	    milestoneVO.setUserCode(userCode);
	    milestoneVO.setProjectId(projectId);
	    
	    if (milestoneVO.getMilestoneStatus() == null) {
	        milestoneVO.setMilestoneStatus("g1"); 
	    }
	    
	    milestoneService.addMilestone(milestoneVO, taskIds);
	    
	    return "redirect:/project/milestone?projectId=" + projectId;
	}

	// 수정 페이지 조회 
	@GetMapping("/update")
	public String milestoneUpdateForm(@AuthenticationPrincipal LoginUserDetails loginUser, // [추가]
									  @RequestParam Long projectId, 
									  @RequestParam Long milestoneId, 
									  Model model) {
		// 권한 체크 수행
		Set<String> permissionCodes = findMilestonePermissionCodes(loginUser, projectId);
		if (!hasMilestonePermission(permissionCodes, PERMISSION_MILESTONE_CREATE_UPDATE_DELETE)) {
			return "weple/access-denide";
		}
		addMilestonePermissionAttributes(model, permissionCodes);

	    model.addAttribute("currentMenu", "milestone");
	    model.addAttribute("sidebarMenu", "project");
	    model.addAttribute("projectId", projectId);
	    model.addAttribute("milestoneId", milestoneId);
	    model.addAttribute("project", projectService.findById(String.valueOf(projectId)));
	    
	    MilestoneVO milestone = milestoneService.getMilestoneInfoById(milestoneId);
	    model.addAttribute("milestone", milestone);
	    
	    List<TaskVO> connectedTaskList = milestoneService.getConnectedTaskList(milestoneId);
	    model.addAttribute("connectedTaskList", connectedTaskList);
	    
	    List<TaskTypeVO> taskTypeList = milestoneService.getTaskTypeList();
	    model.addAttribute("taskTypeList", taskTypeList);
	    
	    List<MilestoneVO> parentMilestoneList = milestoneService.getMilestoneListForUpdate(projectId, milestoneId);
	    model.addAttribute("parentMilestoneList", parentMilestoneList);
	    
	    ProjectVO project = milestoneService.findById(projectId); 
	    
	    model.addAttribute("projectId", projectId);
	    model.addAttribute("projectStart", project.getCreatedAt()); 
	    model.addAttribute("projectFinish", project.getFinishDate());
	    
	    return "weple/milestone/update"; 
	}

	// 마일스톤 및 연결 일감 수정하기
	@PostMapping("/update")
	public String milestoneUpdate(
			@AuthenticationPrincipal LoginUserDetails loginUser, // [추가]
	        @RequestParam Long projectId,
	        @RequestParam Long milestoneId,
	        @RequestParam(value = "taskIds", required = false) List<String> taskIds, 
	        MilestoneVO milestoneVO) {
	    
		// 권한 체크 수행
		Set<String> permissionCodes = findMilestonePermissionCodes(loginUser, projectId);
		if (!hasMilestonePermission(permissionCodes, PERMISSION_MILESTONE_CREATE_UPDATE_DELETE)) {
			return "weple/access-denide";
		}

	    milestoneVO.setProjectId(projectId);
	    milestoneService.modifyMilestone(milestoneVO, taskIds);
	    
	    return "redirect:/project/milestone/detail?projectId=" + projectId + "&milestoneId=" + milestoneId;
	}
	
	// 미지정 일감 비동기 조회 (수정 폼 모달용)
	@GetMapping("/unassigned-tasks")
	@ResponseBody
	public Map<String, Object> getUnassignedTasks(
			@AuthenticationPrincipal LoginUserDetails loginUser, // [추가]
	        @RequestParam("projectId") Long projectId,
	        @RequestParam(value = "milestoneId", required = false) Long milestoneId, 
	        @RequestParam(value = "page", defaultValue = "1") int page,
	        @RequestParam(value = "taskStatus", required = false) String taskStatus,
	        @RequestParam(value = "priority", required = false) String priority,
	        @RequestParam(value = "taskManager", required = false) String taskManager,
	        @RequestParam(value = "typeId", required = false) Long typeId) {
		
		Set<String> permissionCodes = findMilestonePermissionCodes(loginUser, projectId);
				
		// [수정] 논리 오류 교정: 전체 편집(k3_edit)과 내 일감 편집(k3_myedit)이 '둘 다 없는 경우'에만 차단해야 합니다.
		if (!hasMilestonePermission(permissionCodes, PERMISSION_TASK_UPDATE) && 
			!hasMilestonePermission(permissionCodes, PERMISSION_TASK_MYUPDATE)) {
					
		// [수정] 리턴 타입이 Map이므로, 문자열(뷰네임)을 반환하면 컴파일 에러가 납니다. JSON 대응 에러 메시지 반환.
		Map<String, Object> errorResult = new HashMap<>();
		errorResult.put("error", "해당 프로젝트의 일감 편집 권한이 없습니다.");
		return errorResult;
		}
	        
		
	    int pageSize = 10;
	    int startRow = (page - 1) * pageSize + 1;
	    int endRow = page * pageSize;
	    
	    List<TaskVO> taskList = milestoneService.getUnassignedTaskList(projectId, milestoneId, startRow, endRow, taskStatus, priority, taskManager, typeId);
	    int totalCount = milestoneService.getUnassignedTaskCount(projectId, milestoneId, taskStatus, priority, taskManager, typeId);
	    
	    Map<String, Object> result = new HashMap<>();
	    result.put("list", taskList);
	    result.put("totalCount", totalCount);
	    
	    return result;
	}

	// 상위 마일스톤 수정하기
	@PostMapping("/update-parent")
	public String parentMilestoneUpdate(@AuthenticationPrincipal LoginUserDetails loginUser, // [추가]
										@RequestParam Long projectId, 
										MilestoneVO milestoneVO) {
		// 권한 체크 수행
		Set<String> permissionCodes = findMilestonePermissionCodes(loginUser, projectId);
		if (!hasMilestonePermission(permissionCodes, PERMISSION_MILESTONE_CREATE_UPDATE_DELETE)) {
			return "weple/access-denide";
		}

		milestoneVO.setProjectId(projectId);
		milestoneService.updateParentMilestone(milestoneVO);
		
		return "redirect:/project/milestone?projectId=" + projectId;
	}

	// 삭제하기 
	@PostMapping("/delete")
	public String milestoneDelete(@AuthenticationPrincipal LoginUserDetails loginUser, // [추가]
								  @RequestParam Long projectId, 
								  @RequestParam Long milestoneId) {
		// 권한 체크 수행
		Set<String> permissionCodes = findMilestonePermissionCodes(loginUser, projectId);
		if (!hasMilestonePermission(permissionCodes, PERMISSION_MILESTONE_CREATE_UPDATE_DELETE)) {
			return "weple/access-denide";
		}

		milestoneService.deleteMilestone(milestoneId);
		
		return "redirect:/project/milestone?projectId=" + projectId;
	}

	/* ================= 팀원 양식 맞춤 권한 체크 유틸리티 메서드 [추가] ================= */

	/**
	 * 기업 최고관리자/관리자면 전체 허용, 일반 사용자면 프로젝트별 권한 목록 조회
	 */
	private Set<String> findMilestonePermissionCodes(LoginUserDetails loginUser, Long projectId) {
		if (loginUser == null || loginUser.getLoginUser() == null) {
			return Set.of();
		}
		LoginUserVO user = loginUser.getLoginUser();
		if (isCompanyManager(user)) {
	        return Set.of(
	            PERMISSION_MILESTONE_CREATE_UPDATE_DELETE, // k1_version
	            PERMISSION_TASK_CREATE,                    // k3_add
	            PERMISSION_TASK_UPDATE,                    // k3_edit
	            PERMISSION_TASK_MYUPDATE                   // k3_myedit
	        );
	    }
		// 일반 팀원은 프로젝트 단위로 맵핑된 세부 권한 코드를 DB에서 긁어옴
		return repositoryService.findProjectPermissionCodes(user.getUserCode(), projectId);
	}

	/**
	 * 조회된 권한 목록에 필요한 권한 코드가 있는지 확인
	 */
	private boolean hasMilestonePermission(Set<String> permissionCodes, String permissionCode) {
		return permissionCodes != null && permissionCodes.contains(permissionCode);
	}

	/**
	 * 회사 최고관리자(Owner) 또는 시스템 관리자(Admin) 여부 확인
	 */
	private boolean isCompanyManager(LoginUserVO user) {
		return Integer.valueOf(1).equals(user.getOwnerYn()) || Integer.valueOf(1).equals(user.getAdminYn());
	}

	/**
	 * Thymeleaf 화면단에서 등록/수정/삭제 버튼 노출 여부를 제어할 수 있도록 모델에 담음
	 */
	private void addMilestonePermissionAttributes(Model model, Set<String> permissionCodes) {
		model.addAttribute("canManageMilestone", hasMilestonePermission(permissionCodes, PERMISSION_MILESTONE_CREATE_UPDATE_DELETE));
	}
}