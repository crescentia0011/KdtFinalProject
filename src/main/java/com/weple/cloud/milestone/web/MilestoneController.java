package com.weple.cloud.milestone.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.weple.cloud.auth.service.LoginUserDetails;
import com.weple.cloud.milestone.service.MilestoneDetailVO;
import com.weple.cloud.milestone.service.MilestoneInfoVO;
import com.weple.cloud.milestone.service.MilestoneService;
import com.weple.cloud.milestone.service.MilestoneVO;
import com.weple.cloud.project.service.ProjectService;
import com.weple.cloud.project.service.ProjectVO;
import com.weple.cloud.system.service.TaskTypeVO;
import com.weple.cloud.task.service.TaskVO;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/project/milestone") 
public class MilestoneController {

	private final MilestoneService milestoneService;
	
	private final ProjectService projectService;

	// 마일스톤 전체 조회
	@GetMapping
	public String milestoneList(@RequestParam Long projectId, Model model) {
		List<MilestoneInfoVO> list = milestoneService.selectMilestoneAll(projectId); 
		
		model.addAttribute("currentMenu", "milestone");
		model.addAttribute("sidebarMenu", "project");
		model.addAttribute("projectId", projectId);
		model.addAttribute("project", projectService.findById(String.valueOf(projectId)));
		model.addAttribute("milestones", list);
		
		return "weple/milestone/list"; 
	}
	
	// 마일스톤 상세조회
	@GetMapping("/detail")
    public String getMilestoneDetail(@RequestParam Long projectId,
    								 @RequestParam Long milestoneId,
                                     @RequestParam(value = "page", defaultValue = "1") int page,
                                     Model model) {
        // 1. 마일스톤 상세 정보 및 4대 분류 통계 통합 조회
        MilestoneDetailVO detailInfo = milestoneService.getMilestoneDetailInfo(projectId, milestoneId);
        
        // 2. 연결된 일감 리스트 페이징 조회 (한 페이지 최대 20개, 최신순 정렬)
        int pageSize = 20;
        List<TaskVO> paginatedTasks = milestoneService.getMilestoneTasksWithPaging(projectId, milestoneId, page, pageSize);
        
        // DB에서 매핑되어 넘어온 총 일감 개수를 바로 활용 (Stream 연산 제거로 최적화)
        int totalTaskCount = detailInfo.getTotalTaskCount();

        model.addAttribute("projectId", projectId); 
        model.addAttribute("detail", detailInfo);
        model.addAttribute("taskList", paginatedTasks);
        model.addAttribute("currentPage", page);
        model.addAttribute("sidebarMenu", "project");
        model.addAttribute("project", projectService.findById(String.valueOf(projectId)));
        model.addAttribute("totalTasks", totalTaskCount);
        model.addAttribute("totalPages", (int) Math.ceil((double) totalTaskCount / pageSize));

        return "weple/milestone/detail"; // 상세조회 Thymeleaf 경로
    }
	
	// 버전 등록 페이지 조회
		@GetMapping("/version/insert")
		public String versionInsertForm(@RequestParam Long projectId, Model model) {
		    model.addAttribute("currentMenu", "milestone");
		    model.addAttribute("sidebarMenu", "project");
		    model.addAttribute("projectId", projectId);
		    model.addAttribute("project", projectService.findById(String.valueOf(projectId)));
		    
		    ProjectVO project = milestoneService.findById(projectId); 
		    
		    model.addAttribute("projectId", projectId);
		    // 날짜 포맷이 yyyy-MM-dd 형태여야 HTML date 인풋에 호환됩니다.
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
		    
		    String userCode = loginUser.getLoginUser().getUserCode();
		    milestoneVO.setUserCode(userCode);
		    milestoneVO.setProjectId(projectId);
		    
		    if (milestoneVO.getMilestoneStatus() == null) {
		        milestoneVO.setMilestoneStatus("g1"); 
		    }
		    
		 // [변경] 수집된 일감 ID 배열을 서비스 레이어로 위임하여 일괄 처리
		    milestoneService.addVersion(milestoneVO);
		    		    
		    return "redirect:/project/milestone?projectId=" + projectId;
		}

	// 마일스톤 등록 페이지 조회
	@GetMapping("/insert")
	public String milestoneInsertForm(@RequestParam Long projectId, Model model) {
	    model.addAttribute("currentMenu", "milestone");
	    model.addAttribute("sidebarMenu", "project");
	    model.addAttribute("projectId", projectId);
	    model.addAttribute("project", projectService.findById(String.valueOf(projectId)));
	    
	    // 일감유형 목록 조회
	    List<TaskTypeVO> taskTypeList = milestoneService.getTaskTypeList();
	    model.addAttribute("taskTypeList", taskTypeList);
	    
	    List<MilestoneVO> parentMilestoneList = milestoneService.getMilestoneListByProjectId(projectId);
	    model.addAttribute("parentMilestoneList", parentMilestoneList);
	    
	    ProjectVO project = milestoneService.findById(projectId); 
	    
	    model.addAttribute("projectId", projectId);
	    // 날짜 포맷이 yyyy-MM-dd 형태여야 HTML date 인풋에 호환됩니다.
	    model.addAttribute("projectStart", project.getCreatedAt()); 
	    model.addAttribute("projectFinish", project.getFinishDate());
	    
	    return "weple/milestone/register";
	}

	// 마일스톤 등록하기 
	@PostMapping("/insert")
	public String milestoneInsert(
	        @RequestParam Long projectId,
	        @RequestParam(value = "taskIds", required = false) List<String> taskIds, // [추가] 리스트로 수집
	        @AuthenticationPrincipal LoginUserDetails loginUser, 
	        MilestoneVO milestoneVO) {
	    
	    String userCode = loginUser.getLoginUser().getUserCode();
	    milestoneVO.setUserCode(userCode);
	    milestoneVO.setProjectId(projectId);
	    
	    if (milestoneVO.getMilestoneStatus() == null) {
	        milestoneVO.setMilestoneStatus("g1"); 
	    }
	    
	    // [변경] 수집된 일감 ID 배열을 서비스 레이어로 위임하여 일괄 처리
	    milestoneService.addMilestone(milestoneVO, taskIds);
	    
	    return "redirect:/project/milestone?projectId=" + projectId;
	}
	
	

	// 수정 페이지 조회 
		@GetMapping("/update")
		public String milestoneUpdateForm(@RequestParam Long projectId, @RequestParam Long milestoneId, Model model) {
		    model.addAttribute("currentMenu", "milestone");
		    model.addAttribute("sidebarMenu", "project");
		    model.addAttribute("projectId", projectId);
		    model.addAttribute("milestoneId", milestoneId);
		    model.addAttribute("project", projectService.findById(String.valueOf(projectId)));
		    
		    // 1. 수정할 마일스톤의 기존 상세 정보 조회
		    MilestoneVO milestone = milestoneService.getMilestoneInfoById(milestoneId);
		    model.addAttribute("milestone", milestone);
		    
		    List<TaskVO> connectedTaskList = milestoneService.getConnectedTaskList(milestoneId);
		    model.addAttribute("connectedTaskList", connectedTaskList);
		    
		    // 2. 일감유형 목록 조회 (모달 필터용)
		    List<TaskTypeVO> taskTypeList = milestoneService.getTaskTypeList();
		    model.addAttribute("taskTypeList", taskTypeList);
		    
		    // 3. 상위 마일스톤 목록 조회 (자기 자신은 제외하도록 milestoneId 추가 전달)
		    List<MilestoneVO> parentMilestoneList = milestoneService.getMilestoneListForUpdate(projectId, milestoneId);
		    model.addAttribute("parentMilestoneList", parentMilestoneList);
		    
		    ProjectVO project = milestoneService.findById(projectId); 
		    
		    model.addAttribute("projectId", projectId);
		    // 날짜 포맷이 yyyy-MM-dd 형태여야 HTML date 인풋에 호환됩니다.
		    model.addAttribute("projectStart", project.getCreatedAt()); 
		    model.addAttribute("projectFinish", project.getFinishDate());
		    
		    return "weple/milestone/update"; // register.html 복사해서 만들 수정 폼 파일명
		}

		// 마일스톤 및 연결 일감 수정하기
		@PostMapping("/update")
		public String milestoneUpdate(
		        @RequestParam Long projectId,
		        @RequestParam(value = "taskIds", required = false) List<String> taskIds, // 수정된 일감 ID 리스트
		        MilestoneVO milestoneVO) {
		    
		    milestoneVO.setProjectId(projectId);
		    
		    // 서비스단에서 마일스톤 수정 + 일감 초기화 후 재등록을 한 번에 처리
		    milestoneService.modifyMilestone(milestoneVO, taskIds);
		    
		    return "redirect:/project/milestone?projectId=" + projectId;
		}
		
		// 미지정 일감 비동기 조회 (수정 폼 모달용: 이미 내게 엮인 일감도 함께 조회되도록 milestoneId 추가)
		@GetMapping("/unassigned-tasks")
		@ResponseBody
		public Map<String, Object> getUnassignedTasks(
		        @RequestParam("projectId") Long projectId,
		        @RequestParam(value = "milestoneId", required = false) Long milestoneId, // [추가]
		        @RequestParam(value = "page", defaultValue = "1") int page,
		        @RequestParam(value = "taskStatus", required = false) String taskStatus,
		        @RequestParam(value = "priority", required = false) String priority,
		        @RequestParam(value = "taskManager", required = false) String taskManager,
		        @RequestParam(value = "typeId", required = false) Long typeId) {
		        
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
	public String parentMilestoneUpdate(@RequestParam Long projectId, MilestoneVO milestoneVO) {
		milestoneVO.setProjectId(projectId);
		milestoneService.updateParentMilestone(milestoneVO);
		
		return "redirect:/project/milestone?projectId=" + projectId;
	}

	// 삭제하기 
	@PostMapping("/delete")
	public String milestoneDelete(@RequestParam Long projectId, @RequestParam Long milestoneId) {
		milestoneService.deleteMilestone(milestoneId);
		
		return "redirect:/project/milestone?projectId=" + projectId;
	}
}
