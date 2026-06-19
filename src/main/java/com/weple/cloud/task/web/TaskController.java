package com.weple.cloud.task.web;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.weple.cloud.task.service.TaskService;
import com.weple.cloud.task.service.TaskVO;

import lombok.RequiredArgsConstructor;


@Controller
@RequiredArgsConstructor
public class TaskController {
    
	private final TaskService taskService;
	
	@GetMapping("/task")
    public String projectTaskList(Model model) {
		List<TaskVO> list = taskService.findAll();
		model.addAttribute("currentMenu", "task");
		model.addAttribute("taskListinfo",list);
        return "weple/task/list";
    }
	
	@GetMapping("/task/insert")
    public String projectTaskListInsert(Model model) {
		//프로젝트 내부 일감 조회(본인 일감 조건 아직 X)
		model.addAttribute("currentMenu", "task");
		// 일감유형
		model.addAttribute("typeList", taskService.findType());
	    //일감상태
	    model.addAttribute("statusList", taskService.findStatus());
	    //프로젝트 참여 인원
	    model.addAttribute("memberList", taskService.findMember(1)); 
	    //부모 일감 리스트 (상위 일감 선택용)
	    model.addAttribute("parentTaskList", taskService.findParent());
        return "weple/task/register";
    }
	
	@PostMapping("/task/insert")
	public String taskInsertProcess(TaskVO taskVO) {
	    
	    // 프로젝트 선택하는 부분 없어서 정적값 1로 대체
	    taskVO.setProjectId(1); 
	    
	    // 로그인한 당사자 아이디 없어서 정적값 대체 - 이개발계정 아이디
	    taskVO.setUserCode("USR-260618_2"); 

	    taskService.insertTask(taskVO);
	    
	    return "redirect:/task"; // 완료 후 목록으로 리다이렉트
	}


}
