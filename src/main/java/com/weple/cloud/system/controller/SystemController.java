package com.weple.cloud.system.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.weple.cloud.auth.service.LoginUserDetails;
import com.weple.cloud.system.service.CodeValueService;
import com.weple.cloud.system.service.GroupService;
import com.weple.cloud.system.service.RoleService;
import com.weple.cloud.system.service.SystemGroupUserVO;
import com.weple.cloud.system.service.SystemGroupVO;
import com.weple.cloud.system.service.SystemProjectService;
import com.weple.cloud.system.service.SystemProjectVO;
import com.weple.cloud.system.service.TaskTypeService;
import com.weple.cloud.system.service.TaskTypeVO;
import com.weple.cloud.system.service.UserService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class SystemController {

	private final GroupService groupService;
	private final UserService userService;
	private final CodeValueService codeValueService;

	// ---------------------------- 그룹 종류 --------------------------
	// 전체조회
	@GetMapping("groupList")
	public String systemGroupList(@RequestParam(required = false) String keyword, Model model) {
		List<SystemGroupVO> list = groupService.findGroupAll(keyword);
		model.addAttribute("systemGroupList", list);
		model.addAttribute("keyword", keyword);
		model.addAttribute("menu", "group");
		return "weple/admin/group/list";
	}

	// 등록
	@GetMapping("groupInsert")
	public String groupInsertForm() {
		return "weple/admin/group/insert";
	}

	@PostMapping("groupInsert")
	public String groupInsertProcess(SystemGroupVO systemGroupVO) {
		systemGroupVO.setCompanyId(1);
		int gno = groupService.addGroup(systemGroupVO);
		return "redirect:groupList";
	}

	// 삭제
	@GetMapping("groupDelete")
	public String groupDelete(Integer groupId) {
		groupService.removeGroup(groupId);
		return "redirect:groupList";
	}
	


	// ---------------------------- 그룹 내 사용자 --------------------------
	// 전체조회
	@GetMapping("groupUserList")
	public String systemGroupUserList(@RequestParam(value = "groupId", required = false) Integer groupId, Model model) {
		List<SystemGroupUserVO> allList = userService.findGroupUserAll();
		List<SystemGroupUserVO> list = (groupId != null)
				? allList.stream().filter(user -> groupId.equals(user.getGroupId())).toList()
				: allList;
		model.addAttribute("systemGroupUserList", list);
		model.addAttribute("currentGroupUsers", list);
		model.addAttribute("groupId", groupId);
		model.addAttribute("selectedGroupName", getGroupNameDefault(groupId));
		return "weple/admin/group/userList";
	}

	// 그룹 내 사용자 등록 폼
	@GetMapping("groupUserInsert")
	public String groupUserInsertForm(@RequestParam(value = "groupId", required = false) Integer groupId, Model model) {
		List<SystemGroupUserVO> allList = userService.findGroupUserAll();
		List<SystemGroupUserVO> currentGroupUsers = (groupId != null)
				? allList.stream().filter(user -> groupId.equals(user.getGroupId())).toList()
				: new ArrayList<>();
		List<SystemGroupUserVO> availableUsers = allList.stream().filter(user -> user.getGroupId() == null).toList();
		model.addAttribute("currentGroupUsers", currentGroupUsers);
		model.addAttribute("availableUsers", availableUsers);
		model.addAttribute("groupId", groupId);
		model.addAttribute("selectedGroupName", getGroupNameDefault(groupId));
		return "weple/admin/group/userInsert";
	}

	// 그룹 내 사용자 등록 처리
	@PostMapping("groupUserInsert")
	public String groupUserInsertProcess(
			@RequestParam(value = "currentUserIds", required = false) List<String> currentUserIds,
			@RequestParam("groupId") Integer groupId, HttpSession session) {

		Integer companyId = (Integer) session.getAttribute("companyId");

		List<String> userIds = (currentUserIds != null) ? currentUserIds : List.of();
		List<SystemGroupUserVO> allList = userService.findGroupUserAll();

		for (SystemGroupUserVO user : allList) {
			String userCode = user.getUserCode();
			SystemGroupUserVO vo = new SystemGroupUserVO();
			vo.setUserCode(userCode);
			vo.setCompanyId(companyId);

			if (userIds.contains(userCode)) {
				vo.setGroupId(groupId);
				userService.addGroupUser(vo);
			} else if (groupId.equals(user.getGroupId())) {
				vo.setGroupId(0);
				userService.modefyGroupUser(vo);
			}
		}
		return "redirect:/groupList";
	}

	// 그룹 내 사용자 수정 폼
	@GetMapping("groupUserUpdate")
	public String groupUserUpdateForm(@RequestParam("userCode") String userCode, Model model) {
		List<SystemGroupUserVO> allUsers = userService.findGroupUserAll();
		SystemGroupUserVO findVO = allUsers.stream()
				.filter(user -> user.getUserCode() != null && user.getUserCode().equals(userCode)).findFirst()
				.orElse(null);
		model.addAttribute("groupUserUpdate", findVO);
		return "weple/admin/group/userInsert";
	}

	// 그룹 내 사용자 수정 처리
	@PostMapping("groupUserUpdate")
	public String groupUserProcess(SystemGroupUserVO systemGroupUserVO) {
		userService.modefyGroupUser(systemGroupUserVO);
		return "redirect:/groupList";
	}

	// 그룹 내 사용자 삭제
	@GetMapping("groupUserDelete")
	public String groupUserDelete(@RequestParam("userCode") String userCode,
			@RequestParam(value = "groupId", required = false) String groupId) {
		userService.removeGroupUser(userCode);

		if (groupId == null || "null".equals(groupId) || groupId.trim().isEmpty()) {
			return "redirect:/groupList";
		}
		return "redirect:/groupList?groupId=" + groupId;
	}

	// 공통 메서드 - 반복되는 그룹명 조회 로직 분리
	private String getGroupNameDefault(Integer groupId) {
		if (groupId == null)
			return "전체 사용자";

		return groupService.findGroupAll(null).stream().filter(g -> groupId.equals(g.getGroupId()))
				.map(SystemGroupVO::getGroupName).findFirst().orElse("알 수 없는 그룹");
	}

	// -------------------------------일감유형------------------------------
	
	private final TaskTypeService taskTypeService;
	
	// 전체조회
	@GetMapping("/system/taskType")
	public String systemTaskTypeList(@AuthenticationPrincipal LoginUserDetails loginUser, Model model) {
		Long companyId = loginUser.getLoginUser().getCompanyId();
		List<TaskTypeVO> list = taskTypeService.findTaskTypeAll(companyId);
		model.addAttribute("taskTypes", list);
		return "weple/system/taskType/list";
	}

	// 등록페이지 조회
	@GetMapping("/system/taskType/insert")
	public String taskTypeInsert() {
		return "weple/system/taskType/register";
	}

	// 등록하기
	@PostMapping("/system/taskType/insert")
	public String systemTaskTypeInsert(@AuthenticationPrincipal LoginUserDetails loginUser, TaskTypeVO taskTypeVO) {
		Long companyId = loginUser.getLoginUser().getCompanyId();
		taskTypeVO.setCompanyId(companyId);
		taskTypeService.addTaskType(taskTypeVO);
		return "redirect:/system/taskType";
	}

	// 순서 수정하기 (드래그&드랍으로 변경된 순서)
	@PostMapping("/system/taskType/reorder")
	@ResponseBody
	public ResponseEntity<String> systemTaskTypeReorder(@RequestBody List<Integer> sortedIds) {
		taskTypeService.reorderTaskTypes(sortedIds);
		return ResponseEntity.ok("SUCCESS");
	}

	// 수정페이지 조회
	@GetMapping("/system/taskType/update")
	public String taskTypeUpdate(@RequestParam("typeId") int typeId, Model model) {
		TaskTypeVO taskType = taskTypeService.findTaskTypeById(typeId);
		model.addAttribute("taskType", taskType);
		return "weple/system/taskType/register";
	}

	// 수정하기
	@PostMapping("/system/taskType/update")
	public String systemTaskTypeUpdate(TaskTypeVO taskTypeVO) {
		taskTypeService.updateTaskType(taskTypeVO);
		return "redirect:/system/taskType";
	}
	
	// 삭제하기
	@PostMapping("/system/taskType/delete/{typeId}")
	@ResponseBody
	public ResponseEntity<String> systemTaskTypeDelete(@PathVariable("typeId") int typeId) {
		taskTypeService.deleteTaskType(typeId);
		return ResponseEntity.ok("SUCCESS");
	}

	// ---------------------------- 가입승인 --------------------------
	private final com.weple.cloud.system.service.SignupApprovalService signupApprovalService;

	// 현재 로그인한 관리자의 회사에 접수된 승인 대기 회원을 조회합니다.
	@GetMapping("/approvalList")
	public String approvalList(
			@org.springframework.security.core.annotation.AuthenticationPrincipal com.weple.cloud.auth.service.LoginUserDetails loginUser,
			Model model) {
		List<com.weple.cloud.system.service.SignupApprovalUserVO> pendingUsers = signupApprovalService
				.findPendingUsers(loginUser.getLoginUser().getCompanyId());
		model.addAttribute("pendingUsers", pendingUsers);
		// /system/project와 같은 관리 전용 헤더·사이드바 상태를 사용합니다.
		model.addAttribute("sidebarMenu", "system");
		// 프로젝트 탭이 활성화되지 않도록 현재 관리 메뉴를 가입승인으로 지정합니다.
		model.addAttribute("currentMenu", "approval");
		model.addAttribute("menu", "approval");
		return "weple/admin/config/join-request";
	}

	// 요청 URL의 사용자 코드만 받고 회사 정보는 로그인 세션에서 가져와 승인합니다.
	@PostMapping("/approvalList/{userCode}/approve")
	public String approveSignupRequest(
			@org.springframework.security.core.annotation.AuthenticationPrincipal com.weple.cloud.auth.service.LoginUserDetails loginUser,
			@org.springframework.web.bind.annotation.PathVariable String userCode,
			org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
		try {
			signupApprovalService.approvePendingUser(loginUser.getLoginUser().getCompanyId(), userCode);
			redirectAttributes.addFlashAttribute("approvalSuccess", "가입 요청을 승인했습니다.");
		} catch (IllegalArgumentException ex) {
			redirectAttributes.addFlashAttribute("approvalError", ex.getMessage());
		}
		return "redirect:/approvalList";
	}

	// 취소한 승인 대기 가입 요청은 USERS 테이블에서 삭제합니다.
	@PostMapping("/approvalList/{userCode}/cancel")
	public String cancelSignupRequest(
			@org.springframework.security.core.annotation.AuthenticationPrincipal com.weple.cloud.auth.service.LoginUserDetails loginUser,
			@org.springframework.web.bind.annotation.PathVariable String userCode,
			org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
		try {
			signupApprovalService.cancelPendingUser(loginUser.getLoginUser().getCompanyId(), userCode);
			redirectAttributes.addFlashAttribute("approvalSuccess", "가입 요청을 취소했습니다.");
		} catch (IllegalArgumentException ex) {
			redirectAttributes.addFlashAttribute("approvalError", ex.getMessage());
		}
		return "redirect:/approvalList";
	}


	// -------------------------------코드값------------------------------
	// 전체조회
	@GetMapping("codeValueList")
	public String codeValueList(Model model) {

	    model.addAttribute("menu", "code");

	    return "weple/admin/code/list";
	}
	
	@GetMapping("/codeForm") 
    public String showFormPage(Model model) {
        return "weple/admin/code/codeForm";
    }
	
	// -------------------------------프로젝트------------------------------
	
	@Autowired
	private SystemProjectService systemProjectService;
	
	// 프로젝트 조회
	@GetMapping("/system/project/list")
	public String projectList(
			@RequestParam(defaultValue = "1") int page,
	        @RequestParam(required = false) String keyword,
	        @ModelAttribute("toastMessage") String toastMessage,
	        Model model) {
		
		int pageSize = 10;
		
		SystemProjectVO vo = new SystemProjectVO();
	    vo.setPage(page);
	    vo.setPageSize(pageSize);
	    vo.setKeyword(keyword);
	    
	    List<SystemProjectVO> projectList = systemProjectService.selectProjectList(vo);
	    int totalCount = systemProjectService.selectProjectCount(vo);

	    int totalPages = (int) Math.ceil((double) totalCount / pageSize);

	    model.addAttribute("projectList",projectList);
	    model.addAttribute("totalCount", totalCount);
	    model.addAttribute("totalPages", totalPages);
	    model.addAttribute("currentPage", page);
	    model.addAttribute("keyword", keyword);
	    
	    model.addAttribute("sidebarMenu", "system");
	    model.addAttribute("currentMenu", "systemproject");

	    return "weple/system/projectList";
	}
	
	// 프로젝트 생성
	@GetMapping("/system/project")
	public String projectCreateForm(Model model) {
		
		model.addAttribute("sidebarMenu", "system");
		model.addAttribute("currentMenu", "systemproject");
		
		return "weple/system/projectCreate";
	}
	
	@PostMapping("/system/project")
	public String projectCreateProcess(SystemProjectVO projectVO, RedirectAttributes redirectAttributes, Model model) {
		 // 식별자 중복 체크
	    if (systemProjectService.existsByIdentifier(projectVO.getProjectIdentifier())) {
	        redirectAttributes.addFlashAttribute("toastError",
	            "이미 존재하는 식별자입니다: " + projectVO.getProjectIdentifier());
	        return "redirect:/system/project";
	    }
	    
	    // 상태 기본값 세팅
	    projectVO.setStatus("j1");
	    
	    int result = systemProjectService.createProject(projectVO);
	    
		if(result > 0) {
			return "redirect:/project";
		}else {
			model.addAttribute("errorMessage", "프로젝트 생성에 실패했습니다.");
			model.addAttribute("sidebarMenu", "system");
			model.addAttribute("currentMenu", "systemproject");
			
			
			return "weple/system/projectCreate";
		}
	}
	
	// 프로젝트 삭제
	@PostMapping("/system/project/delete")
	public String deleteProject(
			@RequestParam String projectId,
			RedirectAttributes redirectAttributes) {
		
		int result = systemProjectService.deleteProject(projectId);
		
		if(result > 0) {
			redirectAttributes.addFlashAttribute("toastMessage", "프로젝트가 삭제되었습니다.");
		}
		return "redirect:/system/project/list";
	}
	
	// -------------------------------역할 및 권한------------------------------
	@Autowired
	private RoleService roleService;
	
	// 역할 목록
	@GetMapping("/system/role")
	public String roleList(@ModelAttribute("toastMessage") String toastMessage,
							Model model) {
		model.addAttribute("roleList", roleService.selectRoleList());
		model.addAttribute("sidebarMenu", "system");
	    model.addAttribute("currentMenu", "systemrole");
	    return "weple/system/roleList";
	}
	
	// 역할 등록
	
	// 역할 등록 처리
	
	// 역할 삭제
}
