package com.weple.cloud.system.controller;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.weple.cloud.system.service.CodeValueVO;
import com.weple.cloud.system.service.GroupService;
import com.weple.cloud.system.service.GroupUserService;
import com.weple.cloud.system.service.PermissionVO;
import com.weple.cloud.system.service.RoleService;
import com.weple.cloud.system.service.RoleVO;
import com.weple.cloud.system.service.SystemGroupUserVO;
import com.weple.cloud.system.service.SystemGroupVO;
import com.weple.cloud.system.service.SystemModuleService;
import com.weple.cloud.system.service.SystemModuleVO;
import com.weple.cloud.system.service.SystemProjectService;
import com.weple.cloud.system.service.SystemProjectVO;
import com.weple.cloud.system.service.TaskTypeService;
import com.weple.cloud.system.service.TaskTypeVO;
import com.weple.cloud.system.service.UserManagementCreateVO;
import com.weple.cloud.system.service.UserManagementService;
import com.weple.cloud.system.service.UserManagementUpdateVO;
import com.weple.cloud.system.service.UserManagementVO;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class SystemController {

	private final GroupService groupService;
	private final GroupUserService groupuserService;
	private final CodeValueService codeValueService;

	// ---------------------------- 그룹 종류 --------------------------
	// 전체조회
	@GetMapping("groupList")
	public String systemGroupList(@RequestParam(required = false) String keyword, Model model,
			@AuthenticationPrincipal LoginUserDetails loginUser) {
		Long companyId = loginUser.getLoginUser().getCompanyId();
		List<SystemGroupVO> list = groupService.findGroupAll(companyId, keyword);
		model.addAttribute("systemGroupList", list);
		model.addAttribute("keyword", keyword); // 검색어(그룹명)를 화면에 유지하기 위해 전달
		model.addAttribute("menu", "group");
		model.addAttribute("sidebarMenu", "system");
		return "weple/admin/group/list";
	}

	// 등록
	@GetMapping("groupInsert")
	public String groupInsertForm(Model model) {
		model.addAttribute("menu", "group");
		model.addAttribute("sidebarMenu", "system");
		return "weple/admin/group/insert";
	}

	@PostMapping("groupInsert")
	public String groupInsertProcess(SystemGroupVO systemGroupVO, @AuthenticationPrincipal LoginUserDetails loginUser) {
		systemGroupVO.setCompanyId(loginUser.getLoginUser().getCompanyId().intValue());
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
	public String systemGroupUserList(@RequestParam(value = "groupId", required = false) Integer groupId, Model model,
			@AuthenticationPrincipal LoginUserDetails loginUser) {
		List<SystemGroupUserVO> allList = groupuserService.findGroupUserAll(loginUser.getLoginUser().getCompanyId()); // 전체 사용자 목록 가져오기
		List<SystemGroupUserVO> list = (groupId != null) // 그룹 ID가 있다면
				? allList.stream().filter(user -> groupId.equals(user.getGroupId())).toList() // 해당 그룹 사용자만 필터링
				: allList; // 없으면 전체 목록 사용
		model.addAttribute("systemGroupUserList", list); // 필터링된 목록을 화면에 전달
		model.addAttribute("currentGroupUsers", list); // 현재 보고 있는 그룹명 표시
		model.addAttribute("groupId", groupId);
		model.addAttribute("menu", "group");
		model.addAttribute("sidebarMenu", "system");
		model.addAttribute("selectedGroupName", getGroupNameDefault(groupId, loginUser.getLoginUser().getCompanyId()));
		return "weple/admin/group/userList";
	}

	// 그룹 내 사용자 등록 폼
	@GetMapping("groupUserInsert")
	public String groupUserInsertForm(@RequestParam(value = "groupId", required = false) Integer groupId, Model model,
			@AuthenticationPrincipal LoginUserDetails loginUser) {
		List<SystemGroupUserVO> allList = groupuserService.findGroupUserAll(loginUser.getLoginUser().getCompanyId()); // 전체 사용자 목록을 DB에서 가져옴 (allList 변수에 담음)
		List<SystemGroupUserVO> currentGroupUsers = (groupId != null) // 현재 선택된 그룹에 속한 사용자들만 뽑아냄
				// groupId가 있다면 그 그룹 ID와 일치하는 유저만 필터링, 없으면 빈 목록(ArrayList)을 생성
				? allList.stream().filter(user -> groupId.equals(user.getGroupId())).toList()
				: new ArrayList<>();
		// 어느 그룹에도 속하지 않은(groupId가 null인) 사용자들만 뽑아냄
		List<SystemGroupUserVO> availableUsers = allList.stream().filter(user -> user.getGroupId() == null).toList();
		model.addAttribute("currentGroupUsers", currentGroupUsers); // 이미 배정된 유저 리스트
		model.addAttribute("availableUsers", availableUsers); // 배정 가능한 유저 리스트
		model.addAttribute("groupId", groupId); // 현재 선택된 그룹 ID
		model.addAttribute("selectedGroupName", getGroupNameDefault(groupId, loginUser.getLoginUser().getCompanyId())); // 화면에 보여줄 그룹 이름
		model.addAttribute("menu", "group");
		model.addAttribute("sidebarMenu", "system");
		return "weple/admin/group/userInsert";
	}

	// 그룹 내 사용자 등록 처리
	@PostMapping("groupUserInsert")
	public String groupUserInsertProcess(
			// 화면에서 사용자가 선택한 사용자들의 ID 목록을 받아옴 / 값이 없어도 통과 가능
			@RequestParam(value = "currentUserIds", required = false) List<String> currentUserIds,
			@RequestParam("groupId") Integer groupId,
			@AuthenticationPrincipal LoginUserDetails loginUser) {

		Long companyIdLong = loginUser.getLoginUser().getCompanyId(); // 로그인한 사용자 기준 회사 ID
		Integer companyId = companyIdLong != null ? companyIdLong.intValue() : null;

		List<String> userIds = (currentUserIds != null) ? currentUserIds : List.of(); // 화면에서 선택한 유저 리스트 생성
		List<SystemGroupUserVO> allList = groupuserService.findGroupUserAll(companyIdLong); // 전체 사용자 목록 가져옴

		for (SystemGroupUserVO user : allList) {
			String userCode = user.getUserCode();
			SystemGroupUserVO vo = new SystemGroupUserVO();
			vo.setUserCode(userCode);
			vo.setCompanyId(companyId);

			if (userIds.contains(userCode)) {// 선택된 유저라면
				vo.setGroupId(groupId);// 현재 그룹 ID 할당
				groupuserService.addGroupUser(vo);// DB 업데이트
			} else if (groupId.equals(user.getGroupId())) { // 기존엔 포함됐으나 이번에 해제된 유저라면
				vo.setGroupId(0);// 그룹에서 제외(ID를 0으로 변경)
				groupuserService.modefyGroupUser(vo);// DB 업데이트
			}
		}
		return "redirect:/groupList";
	}

	// 그룹 내 사용자 수정 폼
	@GetMapping("groupUserUpdate")
	public String groupUserUpdateForm(@RequestParam("userCode") String userCode, Model model,
			@AuthenticationPrincipal LoginUserDetails loginUser) {
		List<SystemGroupUserVO> allUsers = groupuserService.findGroupUserAll(loginUser.getLoginUser().getCompanyId());
		SystemGroupUserVO findVO = allUsers.stream()
				.filter(user -> user.getUserCode() != null && user.getUserCode().equals(userCode)).findFirst()
				.orElse(null);
		model.addAttribute("groupUserUpdate", findVO);
		return "weple/admin/group/userInsert";
	}

	// 그룹 내 사용자 수정 처리
	@PostMapping("groupUserUpdate")
	public String groupUserProcess(SystemGroupUserVO systemGroupUserVO) {
		groupuserService.modefyGroupUser(systemGroupUserVO);
		return "redirect:/groupList";
	}

	// 그룹 내 사용자 삭제
	@GetMapping("groupUserDelete")
	public String groupUserDelete(@RequestParam("userCode") String userCode,
			@RequestParam(value = "groupId", required = false) String groupId) {
		groupuserService.removeGroupUser(userCode);

		if (groupId == null || "null".equals(groupId) || groupId.trim().isEmpty()) {
			return "redirect:/groupList";
		}
		return "redirect:/groupList?groupId=" + groupId;
	}

	// 공통 메서드 - 반복되는 그룹명 조회 로직 분리
	private String getGroupNameDefault(Integer groupId, Long companyId) {
		if (groupId == null)
			return "전체 사용자";

		return groupService.findGroupAll(companyId, null).stream().filter(g -> groupId.equals(g.getGroupId()))
				.map(SystemGroupVO::getGroupName).findFirst().orElse("알 수 없는 그룹");
	}

	// -------------------------------일감유형------------------------------

	private final TaskTypeService taskTypeService;

	// 전체조회
	@GetMapping("/system/taskType")
	public String systemTaskTypeList(@AuthenticationPrincipal LoginUserDetails loginUser, Model model) {
		Long companyId = loginUser.getLoginUser().getCompanyId();
		List<TaskTypeVO> list = taskTypeService.findTaskTypeAll(companyId);
		model.addAttribute("sidebarMenu", "system");
		model.addAttribute("currentMenu", "taskType");
		model.addAttribute("menu", "taskType");
		model.addAttribute("taskTypes", list);
		return "weple/system/taskType/list";
	}

	// 등록페이지 조회
	@GetMapping("/system/taskType/insert")
	public String taskTypeInsert(Model model) {
		model.addAttribute("sidebarMenu", "system");
		model.addAttribute("currentMenu", "taskType");
		model.addAttribute("menu", "taskType");
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
			@RequestParam(defaultValue = "1") int page,
			Model model) {
		int pageSize = 10;
		int pageBlockSize = 10;
		int totalCount = signupApprovalService.countPendingUsers(loginUser.getLoginUser().getCompanyId());
		int totalPages = Math.max(1, (int) Math.ceil((double) totalCount / pageSize));
		int currentPage = Math.min(Math.max(page, 1), totalPages);
		int offset = (currentPage - 1) * pageSize;
		int startPage = ((currentPage - 1) / pageBlockSize) * pageBlockSize + 1;
		int endPage = Math.min(startPage + pageBlockSize - 1, totalPages);
		List<com.weple.cloud.system.service.SignupApprovalUserVO> pendingUsers = signupApprovalService
				.findPendingUsers(loginUser.getLoginUser().getCompanyId(), offset, pageSize);
		model.addAttribute("pendingUsers", pendingUsers);
		model.addAttribute("totalCount", totalCount);
		model.addAttribute("totalPages", totalPages);
		model.addAttribute("currentPage", currentPage);
		model.addAttribute("pageSize", pageSize);
		model.addAttribute("startPage", startPage);
		model.addAttribute("endPage", endPage);
		// /system/project와 같은 관리 전용 헤더·사이드바 상태를 사용합니다.
		model.addAttribute("sidebarMenu", "system");
		// 프로젝트 탭이 활성화되지 않도록 현재 관리 메뉴를 가입승인으로 지정합니다.
		model.addAttribute("currentMenu", "approval");
		model.addAttribute("menu", "approval");
		return "weple/admin/signup-approval/list";
	}

	// 요청 URL의 사용자 코드만 받고 회사 정보는 로그인 세션에서 가져와 승인합니다.
	@PostMapping("/approvalList/{userCode}/approve")
	public String approveSignupRequest(
			@org.springframework.security.core.annotation.AuthenticationPrincipal com.weple.cloud.auth.service.LoginUserDetails loginUser,
			@org.springframework.web.bind.annotation.PathVariable String userCode,
			@RequestParam(defaultValue = "1") int page,
			org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
		try {
			signupApprovalService.approvePendingUser(loginUser.getLoginUser().getCompanyId(), userCode);
			redirectAttributes.addFlashAttribute("approvalSuccess", "가입 요청을 승인했습니다.");
		} catch (IllegalArgumentException ex) {
			redirectAttributes.addFlashAttribute("approvalError", ex.getMessage());
		}
		return "redirect:/approvalList?page=" + Math.max(page, 1);
	}

	// 취소한 승인 대기 가입 요청은 USERS 테이블에서 삭제합니다.
	@PostMapping("/approvalList/{userCode}/cancel")
	public String cancelSignupRequest(
			@org.springframework.security.core.annotation.AuthenticationPrincipal com.weple.cloud.auth.service.LoginUserDetails loginUser,
			@org.springframework.web.bind.annotation.PathVariable String userCode,
			@RequestParam(defaultValue = "1") int page,
			org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
		try {
			signupApprovalService.cancelPendingUser(loginUser.getLoginUser().getCompanyId(), userCode);
			redirectAttributes.addFlashAttribute("approvalSuccess", "가입 요청을 취소했습니다.");
		} catch (IllegalArgumentException ex) {
			redirectAttributes.addFlashAttribute("approvalError", ex.getMessage());
		}
		return "redirect:/approvalList?page=" + Math.max(page, 1);
	}

	// -------------------------------코드값------------------------------
	// 전체조회
	@GetMapping("codeValueList")
	public String codeValueList(Model model, @AuthenticationPrincipal LoginUserDetails loginUser) {
		Long companyId = loginUser.getLoginUser().getCompanyId();
		List<CodeValueVO> codeList = codeValueService.findCodeValueAll(companyId);
		// 조회 결과가 없으면 빈 리스트 생성해서 넣음
		if (codeList == null) {
			codeList = new java.util.ArrayList<>();
		}
		model.addAttribute("codeList", codeList);
		model.addAttribute("codeList", codeList);
		model.addAttribute("menu", "code");
		model.addAttribute("sidebarMenu", "system");

		return "weple/admin/code/list";
	}

	// 등록 양식
	@GetMapping("/codeInsert")
	//codeInsert?type=work처럼 접속하면 실행, type 값을 받아 화면에 전달
	public String codeInsertForm(@RequestParam("type") String type, Model model) {
		// 타입이 WORK이면 작업분류, 아니면 일감 우선순위
		String pageTitle = "work".equals(type) ? "작업분류" : "일감 우선순위";
	    model.addAttribute("pageTitle", pageTitle);
		model.addAttribute("type", type);
		model.addAttribute("menu", "code");
		model.addAttribute("sidebarMenu", "system");
		// 현재 기본값 이름 (등록 폼에서 기본값 설정 시 confirm 메시지에 표시)
		String defaultName = codeValueService.findDefaultNameByType(type, null);
		model.addAttribute("defaultItemName", (defaultName != null ? defaultName : "없음"));
		return "weple/admin/code/codeForm";
	}

	// 등록 처리
	@PostMapping("codeInsert")
	//용자가 입력한 폼(form)의 값들을 CodeValueVO 객체에 자동으로 담아즘
	public String codeInsertProcess(@ModelAttribute("CodeValue") CodeValueVO codeValueVO, @RequestParam("type") String type, Model model,
			@AuthenticationPrincipal LoginUserDetails loginUser) {
		codeValueVO.setCompanyId(loginUser.getLoginUser().getCompanyId());
		//체크박스는 체크하면 값이 오고, 체크하지 않으면 null
	    codeValueVO.setUsingYn(codeValueVO.getUsingYn() != null ? "Y" : "N");
	    codeValueVO.setDefaultYn(codeValueVO.getDefaultYn() != null ? "Y" : "N");
	    // 기본값으로 등록했으면 다른 기본값은 모두 N로 변경
	    if ("Y".equals(codeValueVO.getDefaultYn())) {
	        codeValueService.resetAllDefaultYn(type); 
	    }
	    
		codeValueService.addCodeValue(codeValueVO, type);
		return "redirect:codeValueList";
	}

	// 수정 양식
	@GetMapping("codeUpdate")
	// CNO=수정할 코드의 번호(ID), TYPE=작업분류인지, 일감 우선순위인지 구분하는 값
	public String codeUpdateForm(@RequestParam("cno") String cno, @RequestParam("type") String type, Model model) {
		CodeValueVO vo = new CodeValueVO();
		//type에 따라 ID 저장
		if ("work".equals(type)) {
	        vo.setTaskClassificationId(cno);
	    } else {
	        vo.setTaskPriorityId(cno);
	    }

	    CodeValueVO result = codeValueService.findCodeValueInfo(vo, type);
	    
	    String defaultName = codeValueService.findDefaultNameByType(type, cno);
	    model.addAttribute("defaultItemName", (defaultName != null ? defaultName : "없음"));

	    String pageTitle = "work".equals(type) ? "작업분류" : "일감 우선순위";
	    model.addAttribute("pageTitle", pageTitle);
	    model.addAttribute("CodeValue", result);
		model.addAttribute("type", type);
		model.addAttribute("menu", "code");
		model.addAttribute("sidebarMenu", "system");
		return "weple/admin/code/codeForm";
	}

	// 수정 처리
	@PostMapping("codeUpdate")
	public String codeUpdateProcess(CodeValueVO codeValueVO, @RequestParam("type") String type, HttpServletRequest request) {
		String defaultYn = (request.getParameter("defaultYn") != null) ? "Y" : "N";
	    codeValueVO.setDefaultYn(defaultYn);
	    String usingYn = (request.getParameter("usingYn") != null) ? "Y" : "N";
	    codeValueVO.setUsingYn(usingYn);
		codeValueService.modifyCodeValue(codeValueVO, type);
	    return "redirect:codeValueList";
	}
	
	//드래그앤드랍
	@PostMapping("/updateOrder")
	@ResponseBody
	public Map<String, Object> updateOrder(@RequestBody Map<String, Object> params,
			@AuthenticationPrincipal LoginUserDetails loginUser) throws Exception {
	    try {
	        String type = (String) params.get("type");
	        List<Map<String, Object>> items = (List<Map<String, Object>>) params.get("items");
	        
	        if (items == null || items.isEmpty()) {
	            throw new Exception("저장할 데이터가 없습니다.");
	        }

	        Long companyId = loginUser.getLoginUser().getCompanyId();
	        List<CodeValueVO> itemList = new ArrayList<>();
	        int order = 1;
	        for (Map<String, Object> item : items) {
	            CodeValueVO vo = new CodeValueVO();
	            vo.setOrderNo(order++);
	            vo.setCompanyId(companyId);
	            if ("work".equals(type)) {
	                vo.setTaskClassificationId(String.valueOf(item.get("id")));
	            } else {
	                vo.setTaskPriorityId(String.valueOf(item.get("id")));
	            }
	            itemList.add(vo);
	        }
	        
	        codeValueService.reorderCodes(type, itemList);
	        
	        Map<String, Object> response = new HashMap<>();
	        response.put("status", "success");
	        return response;
	    } catch (Exception e) {
	        e.printStackTrace();
	        throw e;
	    }
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
			return "redirect:/system/project/list";
		}else {
			model.addAttribute("errorMessage", "프로젝트 생성에 실패했습니다.");
			model.addAttribute("sidebarMenu", "system");
			model.addAttribute("currentMenu", "systemproject");
			
			
			return "weple/system/projectCreate";
		}
	}
	
	// 프로젝트 수정
	@GetMapping("/system/project/update/{projectId}")
	public String projectUpdateForm(
			@PathVariable String projectId,
	        Model model){
				
				SystemProjectVO project = systemProjectService.selectProjectById(Long.parseLong(projectId));
				
				model.addAttribute("project", project);
				model.addAttribute("sidebarMenu", "system");
				model.addAttribute("currentMenu", "systemproject");
				
				return "weple/system/projectUpdate";
			}
	        
	@PostMapping("/system/project/update")
	public String projectUpdateProcess(
			SystemProjectVO projectVO,
			RedirectAttributes redirectAttributes) {
		
		int result = systemProjectService.updateProject(projectVO);
		
		if(result > 0) {
			redirectAttributes.addFlashAttribute("toastMessage", "프로젝트가 수정되었습니다.");
			return "redirect:/system/project/list";
		} else {
			redirectAttributes.addFlashAttribute("toastError", "프로젝트 수정에 실패했습니다.");
			return "redirect:/system/project/update/"+projectVO.getProjectId();
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
	
	// 역할 등록 폼
	@GetMapping("/system/role/create")
	public String roleCreateForm(Model model) {
		model.addAttribute("groupedPermissions", groupPermissions(roleService.selectPermissionList()));
		model.addAttribute("mode", "create");
		
		model.addAttribute("sidebarMenu", "system");
	    model.addAttribute("currentMenu", "systemrole");
	    return "weple/system/roleCreate";
	}
	
	// 역할 수정 폼
	@GetMapping("/system/role/edit")
	public String roleEditForm(@RequestParam Long roleId, Model model) {
		model.addAttribute("role", roleService.selectRoleById(roleId));
		model.addAttribute("checkedCodes", roleService.selectPermissionCodesByRoleid(roleId));
		model.addAttribute("groupedPermissions", groupPermissions(roleService.selectPermissionList()));
		model.addAttribute("mode", "edit");
		
		model.addAttribute("sidebarMenu", "system");
	    model.addAttribute("currentMenu", "systemrole");
	    return "weple/system/roleCreate";
	}
	
	// 역할 등록 처리
	@PostMapping("/system/role/create")
	public String roleCreateProcess(@AuthenticationPrincipal LoginUserDetails loginUser,
									RoleVO roleVO,
									RedirectAttributes redirectAttributes) {
		// 로그인한 관리자 ID 세팅
		roleVO.setCompanyId(loginUser.getLoginUser().getCompanyId());
		int result = roleService.saveRole(roleVO);
		if(result > 0) {
			redirectAttributes.addFlashAttribute("toastMessage", "역할이 등록되었습니다.");
		}
		return "redirect:/system/role";
	}
	
	// 역할 수정 처리
	@PostMapping("/system/role/edit")
	public String roleEditProcess(RoleVO roleVO,
								  RedirectAttributes redirectAttributes) {
		int result = roleService.updateRole(roleVO);
		if(result > 0) {
			redirectAttributes.addFlashAttribute("toastMessage", "역할이 수정되었습니다.");
		}
		return "redirect:/system/role";
	}
	
	// 역할 삭제
	@PostMapping("/system/role/delete")
	public String deleteRole(@RequestParam Long roleId,
			 				 RedirectAttributes redirectAttributes) {
		int result = roleService.deleteRole(roleId);
		if(result > 0) {
			redirectAttributes.addFlashAttribute("toastMessage", "역할이 삭제되었습니다.");
		}
		return "redirect:/system/role";
	}
	
	// private 헬퍼: permissionList → tagLabel 기준 LinkedHashMap
	private java.util.LinkedHashMap<String, java.util.List<PermissionVO>> groupPermissions(
	        java.util.List<PermissionVO> permissionList) {

	    java.util.LinkedHashMap<String, java.util.List<PermissionVO>> map =
	            new java.util.LinkedHashMap<>();

	    for (PermissionVO perm : permissionList) {
	        // tagLabel(한글)이 없으면 permissionTag(k1~k7) 로 폴백
	        String key = (perm.getTagLabel() != null && !perm.getTagLabel().isBlank())
	                     ? perm.getTagLabel()
	                     : perm.getPermissionTag();
	        map.computeIfAbsent(key, k -> new java.util.ArrayList<>()).add(perm);
	    }
	    return map;
	}

	// ---------------------------- 사용자 관리 --------------------------
	@Autowired
	private UserManagementService userManagementService;

	// 현재 로그인한 관리자의 회사에 속한 활성·비활성 사용자 목록을 조회합니다.
	@GetMapping("/userList")
	public String userManagementList(@AuthenticationPrincipal LoginUserDetails loginUser,
			@RequestParam(defaultValue = "1") int page,
			@RequestParam(required = false) String keyword,
			Model model) {
		int pageSize = 10;
		int pageBlockSize = 10;
		Long companyId = loginUser.getLoginUser().getCompanyId();
		String searchKeyword = keyword == null ? null : keyword.trim();
		int totalCount = userManagementService.countUsers(companyId, searchKeyword);
		int totalPages = Math.max(1, (int) Math.ceil((double) totalCount / pageSize));
		int currentPage = Math.min(Math.max(page, 1), totalPages);
		int offset = (currentPage - 1) * pageSize;
		int startPage = ((currentPage - 1) / pageBlockSize) * pageBlockSize + 1;
		int endPage = Math.min(startPage + pageBlockSize - 1, totalPages);

		model.addAttribute("userList", userManagementService.findUsers(companyId, searchKeyword, offset, pageSize));
		model.addAttribute("totalCount", totalCount);
		model.addAttribute("totalPages", totalPages);
		model.addAttribute("currentPage", currentPage);
		model.addAttribute("startPage", startPage);
		model.addAttribute("endPage", endPage);
		model.addAttribute("keyword", searchKeyword);
		model.addAttribute("isCompanyOwner", Integer.valueOf(1).equals(loginUser.getLoginUser().getOwnerYn()));
		model.addAttribute("sidebarMenu", "system");
		model.addAttribute("currentMenu", "systemuser");
		model.addAttribute("menu", "user");
		return "weple/admin/user/list";
	}

	// 신규 사용자 등록 화면으로 이동합니다.
	@GetMapping("/userList/insert")
	public String userManagementInsertForm(Model model) {
		model.addAttribute("sidebarMenu", "system");
		model.addAttribute("currentMenu", "systemuser");
		model.addAttribute("menu", "user");
		return "weple/admin/user/insert";
	}

	// 사용자 목록에서 아이디를 클릭하면 기본 정보와 프로젝트별 역할 정보를 상세조회합니다.
	@GetMapping("/userList/{userCode}")
	public String userManagementDetail(@AuthenticationPrincipal LoginUserDetails loginUser,
			@PathVariable String userCode,
			Model model,
			RedirectAttributes redirectAttributes) {
		Long companyId = loginUser.getLoginUser().getCompanyId();
		UserManagementVO userDetail = userManagementService.findUserDetail(companyId, userCode);
		if (userDetail == null) {
			redirectAttributes.addFlashAttribute("userError", "조회할 사용자를 찾을 수 없습니다.");
			return "redirect:/userList";
		}

		boolean isCompanyOwner = Integer.valueOf(1).equals(loginUser.getLoginUser().getOwnerYn());
		model.addAttribute("userDetail", userDetail);
		model.addAttribute("projectList", userManagementService.findUserProjects(companyId, userCode));
		model.addAttribute("canEditUser", canEditManagedUser(isCompanyOwner, userDetail));
		model.addAttribute("sidebarMenu", "system");
		model.addAttribute("currentMenu", "systemuser");
		model.addAttribute("menu", "user");
		return "weple/admin/user/detail";
	}

	// 사용자 상세조회에서 수정 버튼을 누르면 기본 정보 수정 화면으로 이동합니다.
	@GetMapping("/userList/{userCode}/edit")
	public String userManagementEditForm(@AuthenticationPrincipal LoginUserDetails loginUser,
			@PathVariable String userCode,
			Model model,
			RedirectAttributes redirectAttributes) {
		Long companyId = loginUser.getLoginUser().getCompanyId();
		UserManagementVO userDetail = userManagementService.findUserDetail(companyId, userCode);
		if (userDetail == null) {
			redirectAttributes.addFlashAttribute("userError", "수정할 사용자를 찾을 수 없습니다.");
			return "redirect:/userList";
		}
		boolean isCompanyOwner = Integer.valueOf(1).equals(loginUser.getLoginUser().getOwnerYn());
		if (!canEditManagedUser(isCompanyOwner, userDetail)) {
			redirectAttributes.addFlashAttribute("userError", "관리자 계정은 기업 최고관리자만 수정할 수 있습니다.");
			return "redirect:/userList/" + userCode;
		}

		model.addAttribute("groupList", findCompanyGroups(companyId));
		model.addAttribute("userDetail", userDetail);
		model.addAttribute("isCompanyOwner", isCompanyOwner);
		model.addAttribute("sidebarMenu", "system");
		model.addAttribute("currentMenu", "systemuser");
		model.addAttribute("menu", "user");
		return "weple/admin/user/edit";
	}

	// 사용자 기본정보 수정 화면에서 허용한 항목만 저장하고, 관리자 여부는 기업 최고관리자만 변경할 수 있습니다.
	@PostMapping("/userList/{userCode}/edit")
	public String userManagementEdit(@AuthenticationPrincipal LoginUserDetails loginUser,
			@PathVariable String userCode,
			@ModelAttribute UserManagementUpdateVO user,
			RedirectAttributes redirectAttributes) {
		user.setUserCode(userCode);
		try {
			int actorOwnerYn = Integer.valueOf(1).equals(loginUser.getLoginUser().getOwnerYn()) ? 1 : 0;
			userManagementService.updateUserBasicInfo(loginUser.getLoginUser().getCompanyId(), actorOwnerYn, user);
			redirectAttributes.addFlashAttribute("userSuccess", "사용자 정보가 수정되었습니다.");
			return "redirect:/userList/" + userCode;
		} catch (IllegalArgumentException | IllegalStateException ex) {
			redirectAttributes.addFlashAttribute("userError", ex.getMessage());
			redirectAttributes.addFlashAttribute("userForm", user);
			return "redirect:/userList/" + userCode + "/edit";
		}
	}

	// 화면에서 입력한 신규 사용자 정보를 현재 관리자의 회사 사용자로 등록합니다.
	@PostMapping("/userList/insert")
	public String userManagementInsert(@AuthenticationPrincipal LoginUserDetails loginUser,
			@ModelAttribute UserManagementCreateVO user,
			RedirectAttributes redirectAttributes) {
		try {
			int actorOwnerYn = Integer.valueOf(1).equals(loginUser.getLoginUser().getOwnerYn()) ? 1 : 0;
			userManagementService.createUser(loginUser.getLoginUser().getCompanyId(), actorOwnerYn, user);
			redirectAttributes.addFlashAttribute("userSuccess", "신규 사용자가 등록되었습니다.");
			return "redirect:/userList";
		} catch (IllegalArgumentException | IllegalStateException ex) {
			redirectAttributes.addFlashAttribute("userError", ex.getMessage());
			redirectAttributes.addFlashAttribute("userForm", user);
			return "redirect:/userList/insert";
		}
	}

	// 상태 스위치는 a2(활성)와 a3(비활성) 값만 받아 같은 회사 사용자 상태를 변경합니다.
	@PostMapping("/userList/{userCode}/status")
	public Object changeUserStatus(@AuthenticationPrincipal LoginUserDetails loginUser,
			@PathVariable String userCode,
			@RequestParam String status,
			@RequestParam(defaultValue = "1") int page,
			@RequestParam(required = false) String keyword,
			HttpServletRequest request,
			RedirectAttributes redirectAttributes) {
		boolean ajaxRequest = "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));

		try {
			int actorOwnerYn = Integer.valueOf(1).equals(loginUser.getLoginUser().getOwnerYn()) ? 1 : 0;
			userManagementService.changeUserStatus(loginUser.getLoginUser().getCompanyId(), actorOwnerYn, userCode, status);
			if (ajaxRequest) {
				return ResponseEntity.ok(Map.of(
						"status", status,
						"message", "a2".equals(status) ? "활성화되었습니다." : "비활성화되었습니다."));
			}
			redirectAttributes.addFlashAttribute("userSuccess", "사용자 상태가 변경되었습니다.");
		} catch (IllegalArgumentException ex) {
			if (ajaxRequest) {
				return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
			}
			redirectAttributes.addFlashAttribute("userError", ex.getMessage());
		}
		String searchParameter = keyword == null || keyword.isBlank()
				? "" : "&keyword=" + java.net.URLEncoder.encode(keyword.trim(), java.nio.charset.StandardCharsets.UTF_8);
		return "redirect:/userList?page=" + Math.max(page, 1) + searchParameter;
	}

	private boolean canEditManagedUser(boolean isCompanyOwner, UserManagementVO userDetail) {
		if (isCompanyOwner) {
			return true;
		}
		return !Integer.valueOf(1).equals(userDetail.getOwnerYn())
				&& !Integer.valueOf(1).equals(userDetail.getAdminYn());
	}

	private List<SystemGroupVO> findCompanyGroups(Long companyId) {
		return groupService.findGroupAll(companyId, null);
	}
		
	
	// ---------------------------- 그룹 내 프로젝트 설정 (모듈) --------------------------
	private final SystemModuleService systemModuleService;
	// 전체조회
	@GetMapping("/system/systemModules")
	public String systemInsertModules(@AuthenticationPrincipal LoginUserDetails loginUser, Model model) {
	    Long companyId = loginUser.getLoginUser().getCompanyId();

	    List<SystemModuleVO> moduleList = systemModuleService.findModuleAll();
	    moduleList.sort(Comparator.comparingInt(m -> moduleDisplayOrder(m.getDefaultDescribe())));
	    List<String> enabledCodes = systemModuleService.findEnabledModuleCodes(companyId);
	    List<TaskTypeVO> taskTypeList = taskTypeService.findTaskTypeAll(companyId);
	    List<String> enabledTaskTypeIds = systemModuleService.findEnabledTaskTypeIds(companyId);

	    model.addAttribute("moduleList", moduleList);
	    model.addAttribute("enabledCodes", enabledCodes); // 이미 저장된 것 체크 표시용
	    model.addAttribute("taskTypeList", taskTypeList);
	    model.addAttribute("enabledTaskTypeIds", enabledTaskTypeIds); // 이미 저장된 일감유형 체크 표시용
	    model.addAttribute("sidebarMenu", "system");
	    model.addAttribute("currentMenu", "setting");
	    model.addAttribute("menu", "setting");
	    // 설정 화면 내 하위 탭(프로젝트/저장소) 중 프로젝트 탭 활성 표시함
	    model.addAttribute("settingTab", "project");
	    return "weple/system/systemModules";
	}

	@PostMapping("/system/systemModules")
	public String systemInsertModulesProcess(@AuthenticationPrincipal LoginUserDetails loginUser,
	                                          @RequestParam(value = "enabledModules", required = false) List<String> enabledModules,
	                                          @RequestParam(value = "enabledTaskTypes", required = false) List<String> enabledTaskTypes,
	                                          RedirectAttributes redirectAttributes) {
	    Long companyId = loginUser.getLoginUser().getCompanyId();

	    try {
	        // "개요"/"설정"은 화면에서 항상 체크+비활성(disabled) 상태라 폼에서 아예 값이 안 넘어옴.
	        // 그래서 서버에서 강제로 포함시켜 저장한다.
	        List<String> finalEnabledModules = new ArrayList<>();
	        if (enabledModules != null) {
	            finalEnabledModules.addAll(enabledModules);
	        }
	        for (SystemModuleVO m : systemModuleService.findModuleAll()) {
	            if (("개요".equals(m.getDefaultDescribe()) || "설정".equals(m.getDefaultDescribe()))
	                    && !finalEnabledModules.contains(m.getCommonId())) {
	                finalEnabledModules.add(m.getCommonId());
	            }
	        }

	        systemModuleService.saveEnabledModules(companyId, finalEnabledModules);
	        systemModuleService.saveEnabledTaskTypes(companyId, enabledTaskTypes);
	        redirectAttributes.addFlashAttribute("toastType", "success");
	        redirectAttributes.addFlashAttribute("toastMessage", "모듈 설정이 저장되었습니다.");
	    } catch (Exception e) {
	        redirectAttributes.addFlashAttribute("toastType", "error");
	        redirectAttributes.addFlashAttribute("toastMessage", "저장 중 오류가 발생했습니다.");
	    }

	    return "redirect:/system/systemModules";
	}

	// 새 프로젝트 모듈 설정 화면에 노출할 고정 순서
	// (개요, 작업내역, 마일스톤, 일감, 소요시간, 간트차트, 테스트, 위키, 파일관리, 저장소, 칸반보드, 캘린더, 설정)
	private static final List<String> MODULE_DISPLAY_ORDER = List.of(
	        "개요", "작업내역", "마일스톤", "일감", "소요시간", "간트차트",
	        "테스트", "위키", "파일관리", "저장소", "칸반보드", "캘린더", "설정"
	);

	private int moduleDisplayOrder(String defaultDescribe) {
	    int idx = MODULE_DISPLAY_ORDER.indexOf(defaultDescribe);
	    return idx == -1 ? MODULE_DISPLAY_ORDER.size() : idx;
	}
	
	
}