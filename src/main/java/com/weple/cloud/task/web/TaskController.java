package com.weple.cloud.task.web;

import java.util.List;
import java.util.Map;

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

import com.weple.cloud.auth.service.LoginUserDetails;
import com.weple.cloud.history.task.service.TaskHistoryService;
import com.weple.cloud.project.service.ProjectService;
import com.weple.cloud.task.service.TaskCommentVO;
import com.weple.cloud.task.service.TaskHistoryDTO;
import com.weple.cloud.task.service.TaskProjectSelectVO;
import com.weple.cloud.task.service.TaskService;
import com.weple.cloud.task.service.TaskSpentTimeVO;
import com.weple.cloud.task.service.TaskUpdateHistoryVO;
import com.weple.cloud.task.service.TaskVO;

import lombok.RequiredArgsConstructor;


@Controller
@RequiredArgsConstructor
public class TaskController {
    
	private final TaskService taskService;
	private final ProjectService projectService;
	private final TaskHistoryService taskHistoryService; // 작업내역 일감 불러오기
	
	//프로젝트 내부 일감 목록 페이지 로드
	@GetMapping("/project/task")
    public String projectTaskList(@RequestParam("projectId") Long pId,Model model) {
		

		List<TaskVO> list = taskService.findAll(pId);
		model.addAttribute("projectId", pId);
		model.addAttribute("project", projectService.findById(String.valueOf(pId)));
		model.addAttribute("sidebarMenu", "project");
		model.addAttribute("currentMenu", "task");
		model.addAttribute("taskListinfo",list);
        return "weple/task/list";
    }
	
	//일감 등록 페이지 로드 + 선택 목록 값 로드
	@GetMapping("/project/task/insert")
    public String projectTaskListInsert(@RequestParam("projectId") Long pId,@AuthenticationPrincipal LoginUserDetails loginUser,Model model) {
		String userCode = loginUser.getLoginUser().getUserCode();
	    Long companyId = loginUser.getLoginUser().getCompanyId();
	    
	    //nav 일감 돌아가기 위해서 projectId 넘김
		model.addAttribute("projectId",pId);
	    //내게 할당에서 현재 로그인 정보 확인
	    model.addAttribute("loginUserCode",userCode);
		
		//프로젝트 내부 일감 조회(본인 일감 조건 아직 X)
		model.addAttribute("currentMenu", "task");
		// 일감유형
		model.addAttribute("typeList", taskService.findType(companyId));
	    //일감상태
	    model.addAttribute("statusList", taskService.findStatus());
	    //프로젝트 참여 인원
	    model.addAttribute("memberList", taskService.findMember(pId)); 
	    //우선순위
	    model.addAttribute("priorityList",taskService.findPriority(companyId));
	    //부모 일감 리스트 (상위 일감 선택용)
	    model.addAttribute("parentTaskList", taskService.findParent(pId));
	    
	    model.addAttribute("milestoneList", taskService.findMilestone(pId));
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
	    
	    // 작업내역 등록 - 은지
	    taskHistoryService.insertHistory(
	    	    taskVO.getTaskId(), userCode, "CREATE",
	    	    null, taskVO.getTaskTitle(),   
	    	    null, taskVO.getTypeIdName()
	    	);
	    
	    return "redirect:/project/task?projectId=" + pId;
	}
	
	//일감 상세조회 값 로드
	@GetMapping("/project/task/detail/{tId}")
	public String taskDetail(@PathVariable("tId") String tId,@RequestParam("projectId") Long pId,@AuthenticationPrincipal LoginUserDetails loginUser,Model model,TaskVO taskVO) {
			

		
		TaskVO taskDetail = taskService.findTaskDetail(tId);
		List<TaskCommentVO> taskComment = taskService.findTaskComment(tId);
		List<TaskVO> childTaskList = taskService.findChildTask(tId);
		List<TaskHistoryDTO> updateHistoryList = taskService.taskUpdateHistory(tId);
		List<TaskSpentTimeVO>spentTimeList = taskService.taskSpentTime(tId);
		
		if (loginUser != null && loginUser.getLoginUser() != null) {
	        model.addAttribute("currentUserCode", loginUser.getLoginUser().getUserCode());
	    } else {
	        model.addAttribute("currentUserCode", null);
	    }
		
		model.addAttribute("currentMenu", "task");
		model.addAttribute("projectId",pId);
		model.addAttribute("taskDetail",taskDetail);
		model.addAttribute("chlidTaskList",childTaskList);
		model.addAttribute("taskComment" , taskComment);
		model.addAttribute("updateHistoryList",updateHistoryList);
		model.addAttribute("spentTimeList", spentTimeList);
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
	public String allTaskList(@AuthenticationPrincipal LoginUserDetails loginUser , Model model) {
		String userCode = loginUser.getLoginUser().getUserCode();
		List<TaskVO> list = taskService.findAllList(userCode);
		List<TaskProjectSelectVO> projectList = taskService.findMyProject(userCode);
		model.addAttribute("allList",list);
		model.addAttribute("projectList",projectList);
		return "weple/task/all-list";
		
	}
	
	// 프로젝트 내부 수정 페이지 로드 + 선택 값 목록 로드 + 기존값 로드
	@GetMapping("/project/task/update/{tId}")
	public String taskUpdateForm(@PathVariable("tId") String tId, @RequestParam("projectId") Long pId, @AuthenticationPrincipal LoginUserDetails loginUser, Model model) {
		
		String userCode = loginUser.getLoginUser().getUserCode();
	    Long companyId = loginUser.getLoginUser().getCompanyId();
	    
	    TaskVO taskDetail = taskService.findTaskDetail(tId);
	    
	    //nav 일감 돌아가기 위해서 projectId 넘김
	    model.addAttribute("currentMenu", "task");
	    model.addAttribute("projectId",pId);
	    // 수정할 기존값 넘김
	    model.addAttribute("taskDetail",taskDetail);
	    model.addAttribute("loginUserCode",userCode);
		
		// 일감유형
		model.addAttribute("typeList", taskService.findType(companyId));
	    //일감상태
	    model.addAttribute("statusList", taskService.findStatus());
	    //프로젝트 참여 인원
	    model.addAttribute("memberList", taskService.findMember(pId)); 
	    //우선순위
	    model.addAttribute("priorityList",taskService.findPriority(companyId));
	    //부모 일감 리스트 (상위 일감 선택용)
	    model.addAttribute("parentTaskList", taskService.findParent(pId));
	    
	    model.addAttribute("milestoneList", taskService.findMilestone(pId));
	    
	    System.out.println(taskDetail);
	    
	    return "weple/task/fragment-edit"; // 생성한 수정 페이지 HTML 경로
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
 
	    // 작업내역 저장-은지
	    taskHistoryService.insertHistory(
	    	  taskVO.getTaskId(), userCode, "UPDATE",
	    	  oldTitle, taskVO.getTaskTitle(),      
	    	  oldTypeName, taskVO.getTypeIdName()  
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
	        oldTitle, null,   
	        oldTypeName, null  
	    );
	    
	    // 해당 프로젝트의 일감 목록 페이지로 리다이렉트
	    return "redirect:/project/task?projectId=" + pId;
	}
	
	// 댓글만 ajax 목록 다시 불러오기
	@GetMapping("/api/task/comments/fragment/{tId}")
	public String getCommentFragment(@PathVariable("tId") String tId, Model model, @AuthenticationPrincipal LoginUserDetails loginUser) {
		String userCode = loginUser.getLoginUser().getUserCode();
	    // 최신 댓글 목록 다시 조회
	    List<TaskCommentVO> taskComment = taskService.findTaskComment(tId);
	    model.addAttribute("taskComment", taskComment);
	    
	    // 권한 처리를 위한 로그인 유저 코드 세팅
	    if (loginUser != null && loginUser.getLoginUser() != null) {
	        model.addAttribute("currentUserCode", userCode);
	    } else {
	        model.addAttribute("currentUserCode", null);
	    }
	    
	    // commentArea 부분만 로드
	    return "weple/task/detail :: #commentArea";
	}



}
