package com.weple.cloud.task.web;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import com.weple.cloud.auth.service.LoginUserDetails;
import com.weple.cloud.file.FileDownloadDTO;
import com.weple.cloud.history.task.service.TaskHistoryService;
import com.weple.cloud.notification.service.AlarmType;
import com.weple.cloud.notification.service.NotificationService;
import com.weple.cloud.project.service.ProjectService;
import com.weple.cloud.task.service.TaskCommentVO;
import com.weple.cloud.task.service.TaskHistoryDTO;
import com.weple.cloud.task.service.TaskMemberVO;
import com.weple.cloud.task.service.TaskPermissionVO;
import com.weple.cloud.task.service.TaskProjectSelectVO;
import com.weple.cloud.task.service.TaskService;
import com.weple.cloud.task.service.TaskSpentTimeVO;
import com.weple.cloud.task.service.TaskVO;

import lombok.RequiredArgsConstructor;


@Controller
@RequiredArgsConstructor
public class TaskController {
    
	private final TaskService taskService;
	private final ProjectService projectService;
	private final TaskHistoryService taskHistoryService; // 작업내역 일감 불러오기
	private final NotificationService notificationService;
	
	//프로젝트 내부 일감 목록 페이지 로드
	@GetMapping("/project/task")
	public String projectTaskList(
			@RequestParam(value = "page", defaultValue = "1") int page,
	        @RequestParam("projectId") Long pId,
	        @RequestParam(value = "searchKeyword", required = false) String searchKeyword,
	        @RequestParam(value = "typeIds", required = false) List<Integer> typeIds,
	        @RequestParam(value = "statusIds", required = false) List<String> statusIds, // commonId가 String이므로 수정
	        @RequestParam(value = "priorityNames", required = false) List<String> priorityNames,
	        @RequestParam(value = "memberIds", required = false) List<String> memberIds,
	        @RequestParam(value = "progress", required = false) List<String> progress,
	        @RequestParam(value = "regDate", required = false) List<String> regDate,
	        @RequestParam(value = "dueDate", required = false) List<String> dueDate,
	        Model model, 
	        @AuthenticationPrincipal LoginUserDetails loginUser) {
		
		Integer ownerYn = loginUser.getLoginUser().getOwnerYn();
	    Integer adminYn = loginUser.getLoginUser().getAdminYn();
	    String userCode = loginUser.getLoginUser().getUserCode();
	    Long companyId = loginUser.getLoginUser().getCompanyId();
	    TaskPermissionVO taskPerms = taskService.getTaskPermissions(userCode, pId);
		// 로그인 확인
		if (loginUser == null || loginUser.getLoginUser() == null) {
			return "weple/access-denide";
		}

	    
	    // 관리자 권한을 가지고 있는지
	    boolean isAdminOrOwner = (ownerYn != null && ownerYn == 1) || (adminYn != null && adminYn == 1);

		// 프로젝트 구성원 확인
		List<TaskMemberVO> memberList = taskService.findMember(pId);
		boolean isProjectMember = memberList.stream()
				.anyMatch(member -> userCode.equals(member.getUserCode()));
		
		if (!isProjectMember && !isAdminOrOwner) {
			return "weple/access-denide"; // 구성원이 아니면 접근 불가 페이지 리턴
		}
		
		//페이징 계산을 위한 변수 설정
	    int pageSize = 10; // 한 페이지에 보여줄 일감 개수
	    int offset = (page - 1) * pageSize; // DB에서 가져올 시작 위치

	    Map<String, Object> filterParams = new HashMap<>();
	    filterParams.put("tManager", userCode); // loginUser.getLoginUser().getUserCode() 대신 미리 꺼내둔 변수 사용
	    filterParams.put("projectId", pId);
	    filterParams.put("searchKeyword", searchKeyword);
	    filterParams.put("typeIds", typeIds);
	    filterParams.put("statusIds", statusIds);
	    filterParams.put("priorityNames", priorityNames);
	    filterParams.put("memberIds", memberIds);
	    filterParams.put("progress", progress);
	    filterParams.put("regDate", regDate);
	    filterParams.put("dueDate", dueDate);
	    // DB 쿼리에 전달할 페이징 파라미터 추가
	    filterParams.put("offset", offset);
	    filterParams.put("limit", pageSize);
	    
	    List<TaskVO> list = taskService.findAllWithFilters(filterParams);
	    // 전체 데이터 개수 조회 및 총 페이지 수 계산 (Service/Mapper에 count 쿼리 메서드 추가 필요)
	    int totalCount = taskService.countAllWithFilters(filterParams); 
	    int totalPages = totalCount > 0 ? (int) Math.ceil((double) totalCount / pageSize) : 0;

	    // 화면에 페이징 변수 전달
	    model.addAttribute("currentPage", page);
	    model.addAttribute("totalPages", totalPages);
	    model.addAttribute("projectId", pId);
	    model.addAttribute("loginUserCode", userCode);
	    model.addAttribute("project", projectService.findById(String.valueOf(pId)));
	    model.addAttribute("sidebarMenu", "project");
	    model.addAttribute("currentMenu", "task");
	    
	    // 필터 목록 VO
	    model.addAttribute("typeList", taskService.findType(companyId));
	    model.addAttribute("statusList", taskService.findStatus());
	    model.addAttribute("memberList", memberList); // 권한 체크 시 불러온 목록 재사용
	    model.addAttribute("priorityList", taskService.findPriority(companyId));
	    model.addAttribute("parentTaskList", taskService.findParent(pId));
	    model.addAttribute("taskListinfo", list);
	    model.addAttribute("taskPerms", taskPerms); // 일감관련 가지고있는 권한
	    model.addAttribute("isAdminOrOwner" , isAdminOrOwner);

	    
	    return "weple/task/list";
	}
	
	//일감 등록 페이지 로드 + 선택 목록 값 로드
	@GetMapping("/project/task/insert")
	public String projectTaskListInsert(@RequestParam("projectId") Long pId, 
			@AuthenticationPrincipal LoginUserDetails loginUser, 
			Model model) {
		// 로그인 확인
		if (loginUser == null || loginUser.getLoginUser() == null) {
			return "weple/access-denide";
		}
		
		
		Integer ownerYn = loginUser.getLoginUser().getOwnerYn();
		Integer adminYn = loginUser.getLoginUser().getAdminYn();
		String userCode = loginUser.getLoginUser().getUserCode();
		Long companyId = loginUser.getLoginUser().getCompanyId();
		
		// 최고 권한(소유자/관리자) 확인
				boolean isAdminOrOwner = (ownerYn != null && ownerYn == 1) || (adminYn != null && adminYn == 1);
			    
				// 프로젝트 구성원 확인
				List<TaskMemberVO> memberList = taskService.findMember(pId);
				boolean isProjectMember = memberList.stream()
						.anyMatch(member -> userCode.equals(member.getUserCode()));

				// 최고 관리자도 아니고, 프로젝트 구성원도 아니라면 무조건 즉시 차단
				if (!isAdminOrOwner && !isProjectMember) {
					return "weple/access-denide";
				}

				//  세부 기능 권한(k3Add) 확인
				TaskPermissionVO taskPerms = taskService.getTaskPermissions(userCode, pId);
				boolean isInsertAble = (taskPerms != null && taskPerms.getK3Add() != null);

				// 최고 관리자가 아닌 일반 멤버인데, 일감 등록 권한(k3Add)이 없다면 차단
				// (최고 관리자/소유자는 k3Add 권한이 없어도 마스터 권한으로 통과)
				if (!isAdminOrOwner && !isInsertAble) {
					return "weple/access-denide";
				}

	    // nav 일감 돌아가기 위해서 projectId 넘김
		model.addAttribute("projectId", pId);
	    // 내게 할당에서 현재 로그인 정보 확인
	    model.addAttribute("loginUserCode", userCode);
		
		model.addAttribute("currentMenu", "task");
		// 일감유형
		model.addAttribute("typeList", taskService.findType(companyId));
	    // 일감상태
	    model.addAttribute("statusList", taskService.findStatus());
	    // 프로젝트 참여 인원 (권한 체크 시 불러온 목록 재사용)
	    model.addAttribute("memberList", memberList); 
	    // 우선순위
	    model.addAttribute("priorityList", taskService.findPriority(companyId));
	    // 부모 일감 리스트 (상위 일감 선택용)
	    model.addAttribute("parentTaskList", taskService.findParent(pId));
	    
	    model.addAttribute("milestoneList", taskService.findMilestone(pId));
	    
	    model.addAttribute("sidebarMenu", "project");
	    model.addAttribute("project", projectService.findById(String.valueOf(pId)));

	    
        return "weple/task/register";
    }
	
	//일감 등록 처리
	@PostMapping("/project/task/insert")
	public String taskInsertProcess(@RequestParam("projectId") Long pId,
									@AuthenticationPrincipal LoginUserDetails loginUser,
									TaskVO taskVO,
									@RequestParam(value = "files", required = false) List<MultipartFile> files)throws Exception {
		String userCode = loginUser.getLoginUser().getUserCode();
	    taskVO.setProjectId(pId); 
	    taskVO.setUserCode(userCode); 

	    taskService.insertTask(taskVO, files);
	    TaskVO createdTask = taskService.findTaskDetail(taskVO.getTaskId());
	    // 작업내역 등록 - 은지
	    taskHistoryService.insertHistory(
	    		createdTask.getTaskId(), userCode, "CREATE",
	    	    null, createdTask.getTaskTitle(), 
	    	    null, createdTask.getTaskDescribe(),
	    	    null, createdTask.getTypeIdName(),
	    	    null, createdTask.getTaskStatus(),
	    	    null, createdTask.getTaskManager(),
	    	    null, createdTask.getPriority(),
	    	    null, toStr(createdTask.getStartDate()),
	    	    null, toStr(createdTask.getFinishDate()),
	    	    null, toStr(createdTask.getEstimatedTime()),
	    	    null, toStr(createdTask.getTaskProgress()),
	    	    null, createdTask.getParentTaskTitle()
	    	);
	    
	    return "redirect:/project/task?projectId=" + pId;
	}
	
	//일감 상세조회 값 로드
	@GetMapping("/project/task/detail/{tId}")
	public String taskDetail(@PathVariable("tId") String tId, @RequestParam("projectId") Long pId, @AuthenticationPrincipal LoginUserDetails loginUser, Model model, TaskVO taskVO) {
		if (loginUser == null || loginUser.getLoginUser() == null) {
			return "weple/access-denide";
		}
		Integer ownerYn = loginUser.getLoginUser().getOwnerYn();
		Integer adminYn = loginUser.getLoginUser().getAdminYn();
		String userCode = loginUser.getLoginUser().getUserCode();
		// 로그인 확인
		

		// 관리자 권한을 가지고 있는지
		boolean isAdminOrOwner = (ownerYn != null && ownerYn == 1) || (adminYn != null && adminYn == 1);

		//  프로젝트 구성원 확인
		List<TaskMemberVO> memberList = taskService.findMember(pId);
		boolean isProjectMember = memberList.stream()
				.anyMatch(member -> userCode.equals(member.getUserCode()));
		
		if (!isProjectMember&& !isAdminOrOwner) {
			return "weple/access-denide";
		}
		TaskPermissionVO taskPerms = taskService.getTaskPermissions(userCode, pId);
		TaskVO taskDetail = taskService.findTaskDetail(tId);
		List<TaskCommentVO> taskComment = taskService.findTaskComment(tId);
		List<TaskVO> childTaskList = taskService.findChildTask(tId);
		List<TaskHistoryDTO> updateHistoryList = taskService.taskUpdateHistory(tId);
		List<TaskSpentTimeVO> spentTimeList = taskService.taskSpentTime(tId);
		Long spentSum = spentTimeList.stream()
		        .mapToLong(TaskSpentTimeVO::getSpentHour)
		        .sum();
		
		System.out.println("항목 변경 이력 :" + updateHistoryList);
		
		model.addAttribute("currentUserCode", userCode);
		model.addAttribute("currentMenu", "task");
		model.addAttribute("projectId", pId);
		model.addAttribute("taskDetail", taskDetail);
		model.addAttribute("chlidTaskList", childTaskList);
		model.addAttribute("taskComment", taskComment);
		model.addAttribute("updateHistoryList", updateHistoryList);
		model.addAttribute("spentTimeList", spentTimeList);
		model.addAttribute("spentSum", spentSum);
		model.addAttribute("taskPerms",taskPerms);
		model.addAttribute("isAdminOrOwner",isAdminOrOwner);
		model.addAttribute("sidebarMenu", "project");
		model.addAttribute("project", projectService.findById(String.valueOf(pId)));
		return "weple/task/detail";
	}
	
	//일감 댓글 등록 post 처리
	@PostMapping("/api/task/comments/{tId}")
    @ResponseBody
    public ResponseEntity<?> addComment(@RequestBody TaskCommentVO commentVO,@PathVariable("tId") String tId, @AuthenticationPrincipal LoginUserDetails loginUser) {
        
		String userCode = loginUser.getLoginUser().getUserCode();
	    commentVO.setUserCode(userCode);
	    commentVO.setTaskId(tId);
        
        int result = taskService.insertTaskComment(commentVO);
        
        if (result > 0) {
        	// 알림-은지(댓글 등록)
        	TaskVO task = taskService.findTaskDetail(tId);
        	if(task != null && task.getTaskManagerId() != null
        			&& !task.getTaskManagerId().equals(userCode)) {
        		notificationService.create(
                        task.getTaskManagerId(),
                        AlarmType.TAG_COMMENT,
                        "참여 중인 일감에 댓글이 등록되었습니다.",
                        AlarmType.TARGET_TASK,
                        tId
                  );
        	}
            return ResponseEntity.ok(Map.of("message", "댓글이 등록되었습니다."));
        } else {
            return ResponseEntity.badRequest().body(Map.of("message", "댓글 등록에 실패했습니다."));
        }
    }
	
	// 댓글 수정 처리
	@PutMapping("/api/task/comments/{commentId}")
	@ResponseBody
	public ResponseEntity<?> updateComment(
	        @PathVariable("commentId") Long commentId,
	        @RequestBody TaskCommentVO commentVO,
	        @AuthenticationPrincipal LoginUserDetails loginUser) {
	    

		String userCode = loginUser.getLoginUser().getUserCode();
	    
	    commentVO.setCommentId(commentId);
	    commentVO.setUserCode(userCode); 
	    
	    int result = taskService.updateTaskComment(commentVO);
	    
	    if (result > 0) {
	        return ResponseEntity.ok(Map.of("message", "수정되었습니다."));
	    } else {
	        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "수정 권한이 없거나 실패했습니다."));
	    }
	}


	// 댓글 삭제 처리
	@DeleteMapping("/api/task/comments/{commentId}")
	@ResponseBody
	public ResponseEntity<?> deleteComment(
	        @PathVariable("commentId") Long commentId,
	        @AuthenticationPrincipal LoginUserDetails loginUser) {
	    

	    String userCode = loginUser.getLoginUser().getUserCode();
	    
	    int result = taskService.deleteTaskComment(commentId, userCode);
	    
	    if (result > 0) {
	        return ResponseEntity.ok(Map.of("message", "삭제되었습니다."));
	    } else {
	        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "삭제 권한이 없거나 실패했습니다."));
	    }
	}

	
	// 전체 일감 조회 페이지 로드
	@GetMapping("/task/all-list")
	public String allTaskList(
			@RequestParam(value = "page", defaultValue = "1") int page,
	        @AuthenticationPrincipal LoginUserDetails loginUser,
	        @RequestParam(required = false) String searchKeyword,
	        @RequestParam(required = false) List<String> projectIds,
	        @RequestParam(required = false) List<String> typeIds,
	        @RequestParam(required = false) List<String> statusIds,
	        @RequestParam(required = false) List<String> priorityNames,
	        @RequestParam(required = false) List<String> progress,
	        @RequestParam(required = false) List<String> regDate,
	        @RequestParam(required = false) List<String> dueDate,
	        @RequestParam(required = false) List<String> memberIds,
	        Model model) {

		// 로그인 확인
 		if (loginUser == null || loginUser.getLoginUser() == null) {
 			return "weple/access-denide";
 		}
		
	    String userCode = loginUser.getLoginUser().getUserCode();
	    Long companyId = loginUser.getLoginUser().getCompanyId();

	    Integer ownerYn = loginUser.getLoginUser().getOwnerYn();
	    Integer adminYn = loginUser.getLoginUser().getAdminYn();
	    
	    // 관리자 여부
	 	boolean isAdminOrOwner = (ownerYn != null && ownerYn == 1) || (adminYn != null && adminYn == 1);
	 		 
	    // 페이징 변수
	    int pageSize = 10;
	    int offset = (page - 1) * pageSize;
	    System.out.println(page);
	    System.out.println(offset);
	    
	    Map<String, Object> allParams = new HashMap<>();
	    allParams.put("tManager", userCode);
	    allParams.put("searchKeyword", searchKeyword);
	    allParams.put("projectIds", projectIds);
	    allParams.put("typeIds", typeIds);
	    allParams.put("statusIds", statusIds);
	    allParams.put("priorityNames", priorityNames);
	    allParams.put("progress", progress);
	    allParams.put("regDate", regDate);
	    allParams.put("dueDate", dueDate);
	    allParams.put("memberIds", memberIds);
	    allParams.put("isAdminOrOwner", isAdminOrOwner);
	    // DB 페이징 파라미터
	    allParams.put("offset", offset);
	    allParams.put("limit", pageSize);
	    allParams.put("pageSize", pageSize);
	    
	    // 목록 조회 및 총 개수 구하기
	    List<TaskVO> list;
	    int totalCount = 0;

	    List<TaskProjectSelectVO> projectList = taskService.findMyProject(userCode);
	    List<TaskMemberVO> allMemberList = taskService.findAllMemberList();
	    TaskPermissionVO taskPerms = taskService.getTaskPermissions(userCode, null);
	   
	    
	    list = taskService.findAllMyTasksWithFilters(allParams);
	    totalCount = taskService.countAllMyTasksWithFilters(allParams);
	    // 총 페이지 수 계산
	    int totalPages = totalCount > 0 ? (int) Math.ceil((double) totalCount / pageSize) : 0;
	    
	    model.addAttribute("isAdminOrOwner", isAdminOrOwner); // 화면 제어용 변수 추가
	    //  페이징 화면 전달
	    model.addAttribute("currentPage", page);
	    model.addAttribute("totalPages", totalPages);
	    
	    model.addAttribute("sidebarMenu", "task");
	    model.addAttribute("allList", list);
	    model.addAttribute("projectList", projectList);
	    model.addAttribute("typeList", taskService.findType(companyId));
	    model.addAttribute("statusList", taskService.findStatus());
	    model.addAttribute("priorityList", taskService.findPriority(companyId));
	    model.addAttribute("allMemberList" , allMemberList);
	    model.addAttribute("loginUserCode", userCode);
	    model.addAttribute("taskPerms", taskPerms); // 일감관련 가지고있는 권한
	    model.addAttribute("isAdminOrOwner" , isAdminOrOwner);
	    


	    return "weple/task/all-list";
	}
	
	// 프로젝트 내부 수정 페이지 로드 + 선택 값 목록 로드 + 기존값 로드
	@GetMapping("/project/task/update/{tId}")
	public String taskUpdateForm(@PathVariable("tId") String tId,
	        @RequestParam("projectId") Long pId,
	        @AuthenticationPrincipal LoginUserDetails loginUser,
	        Model model) {

	    // 로그인 확인
	    if (loginUser == null || loginUser.getLoginUser() == null) {
	        return "weple/access-denide";
	    }

	    Integer ownerYn = loginUser.getLoginUser().getOwnerYn();
	    Integer adminYn = loginUser.getLoginUser().getAdminYn();
	    String userCode = loginUser.getLoginUser().getUserCode();
	    Long companyId = loginUser.getLoginUser().getCompanyId();

	    // 최고 관리자 여부
	    boolean isAdminOrOwner =
	            (ownerYn != null && ownerYn == 1) ||
	            (adminYn != null && adminYn == 1);

	    // 프로젝트 멤버 확인
	    List<TaskMemberVO> memberList = taskService.findMember(pId);

	    boolean isProjectMember = memberList.stream()
	            .anyMatch(member -> userCode.equals(member.getUserCode()));

	    if (!isProjectMember && !isAdminOrOwner) {
	        return "weple/access-denide";
	    }

	    // 수정할 일감 조회
	    TaskVO taskDetail = taskService.findTaskDetail(tId);

	    // 수정 권한 조회
	    TaskPermissionVO taskPerms = taskService.getTaskPermissions(userCode, pId);

	    boolean canEditAll =
	            taskPerms != null && taskPerms.getK3Edit() != null;

	    boolean canEditMine =
	            taskPerms != null && taskPerms.getK3Myedit() != null;

	    // 내 일감인지 확인
	    boolean isMyTask =
	            userCode.equals(taskDetail.getTaskManagerId());
	    System.out.println("여기" + taskDetail.getTaskManagerId());

	    // 권한 체크
	    if (!isAdminOrOwner) {

	        // 전체 수정 권한도 없고
	        if (!canEditAll) {

	            // 내 일감 수정 권한도 없거나,
	            // 내 일감 수정 권한은 있지만 내 일감이 아니면 차단
	            if (!canEditMine || !isMyTask) {
	                return "weple/access-denide";
	            }
	        }
	    }

	    model.addAttribute("currentMenu", "task");
	    model.addAttribute("projectId", pId);
	    model.addAttribute("taskDetail", taskDetail);
	    model.addAttribute("loginUserCode", userCode);

	    model.addAttribute("typeList", taskService.findType(companyId));
	    model.addAttribute("statusList", taskService.findStatus());
	    model.addAttribute("memberList", memberList);
	    model.addAttribute("priorityList", taskService.findPriority(companyId));
	    model.addAttribute("parentTaskList", taskService.findParent(pId));
	    model.addAttribute("milestoneList", taskService.findMilestone(pId));
	    model.addAttribute("sidebarMenu", "project");
	    model.addAttribute("project", projectService.findById(String.valueOf(pId)));

	    return "weple/task/fragment-edit";
	}

	// 일감 수정 처리 
	@PostMapping("/project/task/update")
	public String taskUpdateProcess(@RequestParam("projectId") Long pId,
	                                @AuthenticationPrincipal LoginUserDetails loginUser,
	                                TaskVO taskVO,
	                                @RequestParam(value = "files", required = false) List<MultipartFile> files,
	                                @RequestParam(value = "deletedFileIds", required = false) List<Long> deletedFileIds) throws Exception {
	    
		String userCode = loginUser.getLoginUser().getUserCode();
	    taskVO.setProjectId(pId);
	    taskVO.setUserCode(userCode); 

	    // 수정 전 값 먼저 조회-은지
	    TaskVO before = taskService.findTaskDetail(taskVO.getTaskId());
	    String oldTitle = before.getTaskTitle();
	    String oldTypeName = before.getTypeIdName();
	    
	    // 수정 처리 서비스 호출 (VO 내부에 taskId가 hidden으로 담겨서 넘어옵니다)
	    taskService.updateTask(taskVO, files, deletedFileIds);
	    
	    TaskVO after = taskService.findTaskDetail(taskVO.getTaskId());
	    
	    // 알림-은지(상태 변경)
	    boolean statusChanged = before.getTaskStatusId() != null
	            && !before.getTaskStatusId().equals(after.getTaskStatusId());

	    if (statusChanged && after.getTaskManagerId() != null && !after.getTaskManagerId().isBlank()) {
	        notificationService.create(
	            after.getTaskManagerId(),
	            AlarmType.TAG_STATUS_CHANGE,
	            "담당 일감 [" + after.getTaskTitle() + "]의 상태가 변경되었습니다.",
	            AlarmType.TARGET_TASK,
	            after.getTaskId()
	        );
	    }
	    
	    // 알림-은지(첨부파일 등록)
	    if (files != null && !files.isEmpty()
	            && after.getTaskManagerId() != null && !after.getTaskManagerId().isBlank()) {
	        for (MultipartFile file : files) {
	            if (!file.isEmpty()) {
	                notificationService.create(
	                    after.getTaskManagerId(),
	                    AlarmType.TAG_FILE,
	                    "참여 중인 일감에 " + file.getOriginalFilename() + " 파일이 등록되었습니다.",
	                    AlarmType.TARGET_TASK,
	                    after.getTaskId()
	                );
	            }
	        }
	    }
	    
	    // 작업내역 저장-은지
	    taskHistoryService.insertHistory(
	    	  taskVO.getTaskId(), userCode, "UPDATE",
	    	  before.getTaskTitle(),              after.getTaskTitle(),
	    	  before.getTaskDescribe(),           after.getTaskDescribe(),
	          before.getTypeIdName(),             after.getTypeIdName(),
	          before.getTaskStatus(),             after.getTaskStatus(),
	          before.getTaskManager(),            after.getTaskManager(),
	          before.getPriority(),               after.getPriority(),
	          toStr(before.getStartDate()),       toStr(after.getStartDate()),
	          toStr(before.getFinishDate()),      toStr(after.getFinishDate()),
	          toStr(before.getEstimatedTime()),   toStr(after.getEstimatedTime()),
	          toStr(before.getTaskProgress()),    toStr(after.getTaskProgress()),
	          before.getParentTaskTitle(),        after.getParentTaskTitle()
	    	);
	    
	    // 수정 완료 후 해당 일감의 상세조회 페이지로 리다이렉트
	    return "redirect:/project/task/detail/" + taskVO.getTaskId() + "?projectId=" + pId;
	}
	
	// 일감 삭제 소프트 딜리트 처리 (sql update 처리)
	@PostMapping("/project/task/delete/{tId}")
	public String taskDeleteProcess(
	        @RequestParam("projectId") Long pId, 
	        @PathVariable("tId") String tId,
	        @AuthenticationPrincipal LoginUserDetails loginUser) {
	    
	    // 로그인한 유저 코드 가져오기 (누가 삭제했는지 기록)
	    String userCode = loginUser.getLoginUser().getUserCode();
	    
	    // 삭제 전 값 먼저 조회 (이력 저장을 위한 기존 데이터)
	    System.out.println("taskID: " + tId);
	    TaskVO before = taskService.findTaskDetail(tId);
	    System.out.println("aaa:" + before);
	    String oldTitle = before.getTaskTitle();
	    String oldTypeName = before.getTypeIdName();
	    
	    // 소프트 딜리트 실행 (기존 공통 로직)
	    taskService.deleteTask(tId);
	    
	    // 삭제 이력 저장
	    taskHistoryService.insertHistory(
	        tId, userCode, "DELETE",
	        before.getTaskTitle(),              null,
	        before.getTaskDescribe(),           null,
            before.getTypeIdName(),             null,
            before.getTaskStatus(),             null,
            before.getTaskManager(),            null,
            before.getPriority(),               null,
            toStr(before.getStartDate()),       null,
            toStr(before.getFinishDate()),      null,
            toStr(before.getEstimatedTime()),   null,
            toStr(before.getTaskProgress()),    null,
            before.getParentTaskId(),           null
	    );
	    
	    // 해당 프로젝트의 일감 목록 페이지로 리다이렉트
	    return "redirect:/project/task?projectId=" + pId;
	}
	
	// 댓글만 ajax 목록 다시 불러오기
	@GetMapping("/api/task/comments/fragment/{tId}")
	public String getCommentFragment(@PathVariable("tId") String tId,@RequestParam("projectId") Long pId,  Model model, @AuthenticationPrincipal LoginUserDetails loginUser) {
		String userCode = loginUser.getLoginUser().getUserCode();
		if (loginUser != null && loginUser.getLoginUser() != null) {
	        model.addAttribute("currentUserCode", userCode);
	    } else {
	        model.addAttribute("currentUserCode", null);
	    }
		
		
	    Integer ownerYn = loginUser.getLoginUser().getOwnerYn();
	    Integer adminYn = loginUser.getLoginUser().getAdminYn();
	    // 최신 댓글 목록 다시 조회
	    List<TaskCommentVO> taskComment = taskService.findTaskComment(tId);
	    model.addAttribute("taskComment", taskComment);
	    
	    // 권한 처리를 위한 로그인 유저 코드 세팅
	    
	    
	    boolean isAdminOrOwner = (ownerYn != null && ownerYn == 1) || (adminYn != null && adminYn == 1);
	    TaskPermissionVO taskPerms = taskService.getTaskPermissions(userCode, pId);
	    
	    model.addAttribute("isAdminOrOwner", isAdminOrOwner);
	    model.addAttribute("taskPerms", taskPerms);
	    
	    // commentArea 부분만 로드
	    return "weple/task/detail :: #commentArea";
	}
	
	
	 // null 안전 toString 헬퍼 - 은지
    private String toStr(Object obj) {
        return obj == null ? "" : obj.toString();
    }
	//  파일 다운로드
	@GetMapping("/project/download/{versionId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable("versionId") Long versionId) {
	    System.out.println("=== download controller ===");
	    System.out.println("versionId = " + versionId);
		
        // 1. DB에서 조인된 파일 & 버전 정보 조회
        FileDownloadDTO fileInfo = taskService.getFileForDownload(versionId);
        System.out.println("fileInfo = " + fileInfo);
        if (fileInfo == null) {
            return ResponseEntity.notFound().build(); // 삭제됐거나 없는 파일
        }

        try {
            // FILE_PATH = "C:/weple_uploads/tasks", SAVED_NAME = "UUID_images.png" 인 경우를 고려
            Path filePath;
            if (fileInfo.getFilePath().endsWith(fileInfo.getSavedName())) {
                filePath = Paths.get(fileInfo.getFilePath()); // 이미 전체 경로인 경우
            } else {
                filePath = Paths.get(fileInfo.getFilePath(), fileInfo.getSavedName()); // 경로 + 파일명 조합
            }


            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            // 3. LOGICAL_NAME(사용자에게 보여질 원래 이름)으로 한글 깨짐 방지 인코딩
            String encodedFileName = UriUtils.encode(fileInfo.getLogicalName(), StandardCharsets.UTF_8);
            String contentDisposition = "attachment; filename=\"" + encodedFileName + "\"";
            System.out.println("download!! versionId = " + versionId);
            // 4. 응답 객체 생성
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                    .header(HttpHeaders.CONTENT_TYPE, "application/octet-stream")
                    .body(resource);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
