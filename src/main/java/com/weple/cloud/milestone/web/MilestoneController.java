package com.weple.cloud.milestone.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.weple.cloud.auth.service.LoginUserDetails;
import com.weple.cloud.milestone.service.MilestoneDetailVO;
import com.weple.cloud.milestone.service.MilestoneInfoVO;
import com.weple.cloud.milestone.service.MilestoneService;
import com.weple.cloud.milestone.service.MilestoneVO;
import com.weple.cloud.milestone.service.TaskGroupStatVO;
import com.weple.cloud.task.service.TaskVO;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/project/milestone") 
public class MilestoneController {

	private final MilestoneService milestoneService;

	// 마일스톤 전체 조회
	@GetMapping
	public String milestoneList(@RequestParam Long projectId, Model model) {
		List<MilestoneInfoVO> list = milestoneService.selectMilestoneAll(projectId); 
		
		model.addAttribute("currentMenu", "milestone");
		model.addAttribute("projectId", projectId); 
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
        
        // 총 일감 개수 기반 페이징 처리를 위한 전체 카운트 (화면 페이징 네비게이션용)
        int totalTaskCount = 0;
        if (detailInfo.getStatusStats() != null) {
            totalTaskCount = detailInfo.getStatusStats().stream().mapToInt(TaskGroupStatVO::getTotalCount).sum();
        }

        // 3. 화면 데이터 바인딩
        model.addAttribute("detail", detailInfo);
        model.addAttribute("taskList", paginatedTasks);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalTasks", totalTaskCount);
        model.addAttribute("totalPages", (int) Math.ceil((double) totalTaskCount / pageSize));

        return "project/milestone/detail"; // 상세조회 Thymeleaf 경로
    }
	

	// 등록 페이지 조회
	@GetMapping("/insert")
	public String milestoneInsertForm(@RequestParam Long projectId, Model model) {
		model.addAttribute("currentMenu", "milestone");
		model.addAttribute("projectId", projectId);
		
		return "weple/milestone/register";
	}

	// 등록하기 
	@PostMapping("/insert")
	public String milestoneInsert(
			@RequestParam Long projectId,
			@AuthenticationPrincipal LoginUserDetails loginUser, 
			MilestoneVO milestoneVO) {
		
		String userCode = loginUser.getLoginUser().getUserCode();
		milestoneVO.setUserCode(userCode);
		milestoneVO.setProjectId(projectId);
		
		if (milestoneVO.getMilestoneStatus() == null) {
			milestoneVO.setMilestoneStatus("g1"); 
		}
		
		milestoneService.addMilestone(milestoneVO);
		
		return "redirect:/project/milestone?projectId=" + projectId;
	}

	// 수정 페이지 조회
	@GetMapping("/update")
	public String milestoneUpdateForm(
			@RequestParam Long projectId,
			@RequestParam Long milestoneId, 
			Model model) {
		
		MilestoneDetailVO milestone = milestoneService.getMilestoneDetailInfo(projectId, milestoneId);
		
		model.addAttribute("currentMenu", "milestone");
		model.addAttribute("projectId", projectId);
		model.addAttribute("milestone", milestone);
		
		return "weple/milestone/register";
	}

	// 수정하기
	@PostMapping("/update")
	public String milestoneUpdate(@RequestParam Long projectId, MilestoneVO milestoneVO) {
		milestoneVO.setProjectId(projectId);
		milestoneService.updateMilestone(milestoneVO);
		
		return "redirect:/project/milestone?projectId=" + projectId;
	}

	// 삭제하기 
	@PostMapping("/delete/{milestoneId}")
	@ResponseBody
	public ResponseEntity<String> milestoneDelete(@PathVariable("milestoneId") Long milestoneId) {
		int result = milestoneService.deleteMilestone(milestoneId);
		if (result > 0) {
			return ResponseEntity.ok("SUCCESS");
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("FAIL");
		}
	}
}
