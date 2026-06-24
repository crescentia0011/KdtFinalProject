package com.weple.cloud.task.web;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.weple.cloud.auth.service.LoginUserDetails;
import com.weple.cloud.project.service.ProjectService;
import com.weple.cloud.history.task.service.TaskHistoryService;
import com.weple.cloud.task.service.TaskProjectSelectVO;
import com.weple.cloud.task.service.TaskService;
import com.weple.cloud.task.service.TaskVO;

import lombok.RequiredArgsConstructor;


@Controller
@RequiredArgsConstructor
public class TaskController {
    
	private final TaskService taskService;
	private final ProjectService projectService;
	private final TaskHistoryService taskHistoryService; // 작업내역 일감 불러오기-은지
	
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
	
	@PostMapping("/project/task/insert")
	public String taskInsertProcess(@RequestParam("projectId") Long pId,@AuthenticationPrincipal LoginUserDetails loginUser,TaskVO taskVO,@RequestParam(value = "files", required = false) List<MultipartFile> files)throws Exception {
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
	
	@GetMapping("/project/task/detail/{tId}")
	public String taskDetail(@PathVariable("tId") String tId,@RequestParam("projectId") Long pId,@AuthenticationPrincipal LoginUserDetails loginUser,Model model,TaskVO taskVO) {
			
		TaskVO taskDetail = taskService.findTaskDetail(tId);
		List<TaskVO> childTaskList = taskService.findChildTask(tId);
		model.addAttribute("currentMenu", "task");
		model.addAttribute("projectId",pId);
		model.addAttribute("taskDetail",taskDetail);
		model.addAttribute("chlidTaskList",childTaskList);
		return "weple/task/detail";
	}
	
	
	@GetMapping("/task/all-list")
	public String allTaskList(@AuthenticationPrincipal LoginUserDetails loginUser , Model model) {
		String userCode = loginUser.getLoginUser().getUserCode();
		List<TaskVO> list = taskService.findAllList(userCode);
		List<TaskProjectSelectVO> projectList = taskService.findMyProject(userCode);
		model.addAttribute("allList",list);
		model.addAttribute("projectList",projectList);
		return "weple/task/all-list";
		
	}
	
	// 1. 수정 페이지 이동 (GET)
	@GetMapping("/project/task/update/{tId}")
	public String taskUpdateForm(@PathVariable("tId") String tId, @RequestParam("projectId") Long pId, @AuthenticationPrincipal LoginUserDetails loginUser, Model model) {
		
		String userCode = loginUser.getLoginUser().getUserCode();
	    Long companyId = loginUser.getLoginUser().getCompanyId();
	    
	    TaskVO taskDetail = taskService.findTaskDetail(tId);
	    
	    //nav 일감 돌아가기 위해서 projectId 넘김
	    model.addAttribute("currentMenu", "task");
	    model.addAttribute("projectId",pId);
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

	// 2. 수정 처리 (POST)
	@PostMapping("/project/task/update")
	public String taskUpdateProcess(@RequestParam("projectId") Long pId,
	                                @AuthenticationPrincipal LoginUserDetails loginUser,
	                                TaskVO taskVO,
	                                @RequestParam(value = "files", required = false) List<MultipartFile> files) throws Exception {
	    
	    // 프로젝트 ID 설정
		String userCode = loginUser.getLoginUser().getUserCode();
	    taskVO.setProjectId(pId);
	    taskVO.setUserCode(userCode); 
	    
	    // 수정 전 값 먼저 조회-은지
	    TaskVO before = taskService.findTaskDetail(taskVO.getTaskId());
	    String oldTitle = before.getTaskTitle();
	    String oldTypeName = before.getTypeIdName();
	    
	    // 수정 처리 서비스 호출 (VO 내부에 taskId가 hidden으로 담겨서 넘어옵니다)
	    taskService.updateTask(taskVO, files);
 
	    // 작업내역 저장-은지
	    taskHistoryService.insertHistory(
	    	  taskVO.getTaskId(), userCode, "UPDATE",
	    	  oldTitle, taskVO.getTaskTitle(),      
	    	  oldTypeName, taskVO.getTypeIdName()  
	    	);
	    
	    // 수정 완료 후 해당 일감의 상세조회 페이지로 리다이렉트
	    return "redirect:/project/task/detail/" + taskVO.getTaskId() + "?projectId=" + pId;
	}
	
	@DeleteMapping("/project/task/delete")
	public String taskDeleteProcess(
			@RequestParam("projectId") Long pId,
			@RequestParam("tId") String tId,
			@AuthenticationPrincipal LoginUserDetails loginUser) {
		
		// userCode 가져와야 누가 삭제했는지 저장 가능-은지
		String userCode = loginUser.getLoginUser().getUserCode();
		
		// 삭제 전 값 먼저 조회-은지
		TaskVO before = taskService.findTaskDetail(tId);
		String oldTitle = before.getTaskTitle();
	    String oldTypeName = before.getTypeIdName();
	    
	    // 소프트 딜리트-은지
	    taskService.deleteTask(tId);
	    
	 // 삭제 이력 저장-은지
	    taskHistoryService.insertHistory(
	        tId, userCode, "DELETE",
	        oldTitle, null,   
	        oldTypeName, null  
	    );
	    
		
		return "redirect:/project/task" + "?projectId=" +pId;
		
	}


}
