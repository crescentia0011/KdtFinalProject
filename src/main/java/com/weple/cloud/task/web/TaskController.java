package com.weple.cloud.task.web;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.weple.cloud.auth.service.LoginUserDetails;
import com.weple.cloud.task.service.TaskProjectSelectVO;
import com.weple.cloud.task.service.TaskService;
import com.weple.cloud.task.service.TaskVO;

import lombok.RequiredArgsConstructor;


@Controller
@RequiredArgsConstructor
public class TaskController {
    
	private final TaskService taskService;
	
	@GetMapping("/project/task")
    public String projectTaskList(@RequestParam("projectId") Long pId,Model model) {
		

		List<TaskVO> list = taskService.findAll(pId);
		model.addAttribute("currentMenu", "task");
		model.addAttribute("taskListinfo",list);
        return "weple/task/list";
    }
	
	@GetMapping("/project/task/insert")
    public String projectTaskListInsert(@RequestParam("projectId") Long pId,@AuthenticationPrincipal LoginUserDetails loginUser,Model model) {
		String userCode = loginUser.getLoginUser().getUserCode();
	    Long companyId = loginUser.getLoginUser().getCompanyId();
	    
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
	public String taskInsertProcess(@RequestParam("projectId") Long pId,@AuthenticationPrincipal LoginUserDetails loginUser,TaskVO taskVO) {
		String userCode = loginUser.getLoginUser().getUserCode();
	    // 프로젝트 선택하는 부분 없어서 정적값 1로 대체
	    taskVO.setProjectId(pId); 
	    
	    // 로그인한 당사자 아이디 없어서 정적값 대체 - 이개발계정 아이디
	    taskVO.setUserCode(userCode); 

	    taskService.insertTask(taskVO);
	    System.out.println("화면에서 넘어온 TaskVO 데이터: " + taskVO.toString());
	    System.out.println("VO 내부의 담당자(taskManager): " + taskVO.getTaskManager());
	    
	    return "redirect:/project/task?projectId=" + pId;
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


}
