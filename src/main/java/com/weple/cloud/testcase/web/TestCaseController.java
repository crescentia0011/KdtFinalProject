package com.weple.cloud.testcase.web;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.weple.cloud.auth.service.LoginUserDetails;
import com.weple.cloud.task.service.TaskParentVO;
import com.weple.cloud.testcase.service.CoverdStatusVO;
import com.weple.cloud.testcase.service.TestCaseMemberVO;
import com.weple.cloud.testcase.service.TestCasePriorityVO;
import com.weple.cloud.testcase.service.TestCaseService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class TestCaseController {
	
	private final TestCaseService testCaseService;

	// 테스트케이스 리스트 목록
	@GetMapping("/project/testcase")
    public String testCaseList(@RequestParam("projectId") Long pId, Model model) {
        model.addAttribute("currentMenu", "testcase");
        model.addAttribute("projectId", pId); // 다른 메뉴 이동 및 등록용
        model.addAttribute("currentTab", "testcase"); // 탭 활성화 표시용
        return "weple/testcase/list";
    }
	
	//  요구사항 커버리지 리스트 페이지
    @GetMapping("/project/coverage")
    public String coverageList(@RequestParam("projectId") Long pId, Model model) {
        model.addAttribute("currentMenu", "testcase"); // 네비게이션 활성화 유지
        model.addAttribute("projectId", pId);
        model.addAttribute("currentTab", "coverage"); // 탭 활성화 표시용
        return "weple/testcase/coverage"; // 새로 만들 html 파일명
    }
    
    @GetMapping("/project/testcase/insert")
    public String testCaseInsert(@RequestParam("projectId") Long pId,
    							Model model,
    							@AuthenticationPrincipal LoginUserDetails loginUser) {
        
		if (loginUser == null || loginUser.getLoginUser() == null) {
			return "weple/access-denide";
		}
	    String userCode = loginUser.getLoginUser().getUserCode();
	    Long companyId = loginUser.getLoginUser().getCompanyId();
    	
	    List<TestCaseMemberVO> memberList = testCaseService.findTestCaseMembers(pId);
	    List<CoverdStatusVO> statusList = testCaseService.findCoverageStatus();
	    List<TaskParentVO> taskList = testCaseService.findTestCaseTaskList(pId);
	    List<TestCasePriorityVO> priorityList = testCaseService.findTestCasePriorities(companyId);
	    
	    model.addAttribute("currentMenu", "testcase");
    	model.addAttribute("projectId", pId);
    	model.addAttribute("loginUserCode",userCode);
    	model.addAttribute("memberList" ,memberList);
    	model.addAttribute("statusList",statusList);
    	model.addAttribute("taskList",taskList);
    	model.addAttribute("priorityList",priorityList);
    	
    	return "weple/testcase/register";
    }
	
	
	
	
}
