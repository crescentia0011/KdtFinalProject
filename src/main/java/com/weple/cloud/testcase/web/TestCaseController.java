package com.weple.cloud.testcase.web;


import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.weple.cloud.auth.service.LoginUserDetails;
import com.weple.cloud.task.service.TaskParentVO;
import com.weple.cloud.testcase.service.CoverdStatusVO;
import com.weple.cloud.testcase.service.TestCaseMemberVO;
import com.weple.cloud.testcase.service.TestCasePriorityVO;
import com.weple.cloud.testcase.service.TestCaseService;
import com.weple.cloud.testcase.service.TestCaseVO;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class TestCaseController {
	
	private final TestCaseService testCaseService;

	// 테스트케이스 리스트 목록
	@GetMapping("/project/testcase")
	public String testCaseList(@RequestParam("projectId") Long pId, 
	                           Model model,
	                           @RequestParam(value = "page", defaultValue = "1") int page,
	                           @AuthenticationPrincipal LoginUserDetails loginUser,
	                           @RequestParam(value = "searchKeyword", required = false) String searchKeyword) {
	    
	    if (loginUser == null || loginUser.getLoginUser() == null) {
	        return "weple/access-denide";
	    }
	    
	    int pageSize = 10;    // 한 페이지에 보여줄 데이터 개수
	    int blockLimit = 5;  // 하단에 보여줄 페이지 번호 블록 개수
	    int offset = (page - 1) * pageSize; // DB에서 데이터를 건너뛸 기준값 계산

	    // 전체 데이터 개수 조회 (검색어 포함)
	    int totalRecords = testCaseService.getTestCaseCount(pId, searchKeyword);

	    // 총 페이지 수 계산
	    int totalPages = (int) Math.ceil((double) totalRecords / pageSize);

	    //페이징된 데이터 리스트 조회
	    List<TestCaseVO> list = testCaseService.findTestCaseList(pId, searchKeyword, offset, pageSize);

	    // 하단 블록 시작/끝 번호 계산
	    int startPage = (((int)(Math.ceil((double)page / blockLimit))) - 1) * blockLimit + 1;
	    int endPage = startPage + blockLimit - 1;
	    
	    if (endPage > totalPages) {
	        endPage = totalPages; 
	    }
	    if (endPage == 0) {
	        endPage = 1; 
	    }
	    
	    model.addAttribute("testCaseList", list);
	    model.addAttribute("currentPage", page);
	    model.addAttribute("totalPages", totalPages);
	    model.addAttribute("startPage", startPage);
	    model.addAttribute("endPage", endPage);
	    model.addAttribute("searchKeyword", searchKeyword); // 검색어 유지용
	    
	    // 기타 공통 데이터
	    model.addAttribute("currentMenu", "testcase");
	    model.addAttribute("projectId", pId); 
	    model.addAttribute("currentTab", "testcase"); 

	    return "weple/testcase/list";
	}
	
	//  요구사항 커버리지 리스트 페이지
    @GetMapping("/project/coverage")
    public String coverageList(@RequestParam("projectId") Long pId, 
    							Model model,
    							@RequestParam(value = "page", defaultValue = "1") int page,
    							@AuthenticationPrincipal LoginUserDetails loginUser
    							) {
    
    	
    	if (loginUser == null || loginUser.getLoginUser() == null) {
	        return "weple/access-denide";
	    }
    	
    	int pageSize = 10;    // 한 페이지에 보여줄 데이터 개수
	    int blockLimit = 5;  // 하단에 보여줄 페이지 번호 블록 개수
	    int offset = (page - 1) * pageSize; // DB에서 데이터를 건너뛸 기준값 계산

	    // 전체 데이터 개수 조회 (검색어 포함)
	    int totalRecords = testCaseService.getTestCaseCount(pId, null);

	    // 총 페이지 수 계산
	    int totalPages = (int) Math.ceil((double) totalRecords / pageSize);

	    //페이징된 데이터 리스트 조회
	    List<TestCaseVO> list = testCaseService.findTestCaseList(pId, null, offset, pageSize);

	    // 하단 블록 시작/끝 번호 계산
	    int startPage = (((int)(Math.ceil((double)page / blockLimit))) - 1) * blockLimit + 1;
	    int endPage = startPage + blockLimit - 1;
	    
	    if (endPage > totalPages) {
	        endPage = totalPages; 
	    }
	    if (endPage == 0) {
	        endPage = 1; 
	    }
    	
	    model.addAttribute("testCaseList", list);
	    model.addAttribute("currentPage", page);
	    model.addAttribute("totalPages", totalPages);
	    model.addAttribute("startPage", startPage);
	    model.addAttribute("endPage", endPage);
    	
        model.addAttribute("currentMenu", "testcase"); 
        model.addAttribute("projectId", pId);
        model.addAttribute("currentTab", "coverage");
        return "weple/testcase/list";
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
    
    @PostMapping("/project/testcase/register")
    public String registerTestCase(
            // 1. 시큐리티 인증 객체에서 로그인 유저 정보 가져오기
            @AuthenticationPrincipal LoginUserDetails loginUser,
            // 2. URL 파라미터나 폼 숨김 필드로 넘어오는 projectId
            @RequestParam("projectId") Long pId, 
            
            // 3. 폼에서 입력받는 데이터들
            @RequestParam("taskId") String taskId, // 일감 선택에서 넘어온 값
            @RequestParam("title") String title,
            @RequestParam("status") String status,
            @RequestParam(value = "testCaseManager", required = false) String testCaseManager,
            @RequestParam("priority") String priority,
            @RequestParam(value = "testDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date testDate,
            @RequestParam("isTested") String isTested,
            @RequestParam("testResult") String testResult,
            @RequestParam(value = "description", required = false) String description
    ) {
        
        // 로그인한 사용자의 userCode 추출
        String userCode = loginUser.getLoginUser().getUserCode();
        
        // VO 객체 생성 및 데이터 매핑
        TestCaseVO vo = new TestCaseVO();
        
        vo.setTestName(title);
        vo.setCoverageStatus(status);
        vo.setTestManager(testCaseManager);
        vo.setPriority(priority);
        vo.setTestDate(testDate);
        vo.setTestYn(isTested);
        vo.setTestContent(testResult);
        vo.setTestDescribe(description);
        
        // 추가된 3가지 핵심 데이터 세팅
        vo.setTaskId(taskId);
        vo.setUserCode(userCode);
        // VO의 projectId가 String 타입이라면 String.valueOf()로 변환해서 넣어줍니다.
        vo.setProjectId(pId); 

        // 서비스 호출 (insert 실행)
        int result = testCaseService.insetTestCaseService(vo);

        // 등록 완료 후 목록 페이지로 리다이렉트 (경로는 상황에 맞게 수정하세요)
        return "redirect:/project/testcase?projectId=" + pId; 
    }
	
	
	
	
}
