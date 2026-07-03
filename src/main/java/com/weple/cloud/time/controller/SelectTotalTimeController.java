package com.weple.cloud.time.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.weple.cloud.auth.service.LoginUserDetails;
import com.weple.cloud.time.service.SelectTotalTimeService;
import com.weple.cloud.time.service.SelectTotalTimeVO;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class SelectTotalTimeController {
	// -------------------------------전체 소요시간------------------------------
	private final SelectTotalTimeService selectTotalTimeService;

	// 전체조회 (관리자: 소속 회사 전체 프로젝트의 전체 사용자 건 / 일반 사용자: 본인이 속한 프로젝트에서 본인이 등록한 건만)
	@GetMapping("totalTimeList")
	public String totalTimeList(Model model, @AuthenticationPrincipal LoginUserDetails loginUser) {
		boolean isManager = Integer.valueOf(1).equals(loginUser.getLoginUser().getOwnerYn())
				|| Integer.valueOf(1).equals(loginUser.getLoginUser().getAdminYn());
		List<SelectTotalTimeVO> list = selectTotalTimeService.findSelectTotalTimeAll(
				loginUser.getLoginUser().getCompanyId(),
				loginUser.getLoginUser().getUserCode(),
				isManager);
		model.addAttribute("isManager", isManager);
		model.addAttribute("loginUserCode", loginUser.getLoginUser().getUserCode());
		model.addAttribute("totalTimeList", list);
		if (!list.isEmpty()) {
		    model.addAttribute("countSpentHour", list.get(0).getCountSpentHour());
		    model.addAttribute("totalSpentHour", list.get(0).getTotalSpentHour());
		}
		model.addAttribute("sidebarMenu", "time");
		return "weple/time/all-total";
	}

	// 수정 폼
	@GetMapping("/updateTotalTime")
	public String modifyTotalTimeForm(@RequestParam("workId") long workId, Model model) {
		// SelectTotalTimeVO vo = selectTotalTimeService.modifySelectTotalTime(workId);
		// model.addAttribute("totalTime", vo);
		return "weple/time/insert";
	}

	// 수정 처리
	@PostMapping("/updateTotalTime")
	public String modifyTotalTimeProcess(SelectTotalTimeVO selectTotalTimeVO) {
		selectTotalTimeService.modifySelectTotalTime(selectTotalTimeVO);
		return "redirect:/totalTimeList";
	}

	// 삭제 (관리자만 가능)
	@GetMapping("/deleteTotalTime")
	public String deleteWork(@RequestParam("workId") long workId, @AuthenticationPrincipal LoginUserDetails loginUser) {
	    boolean isManager = Integer.valueOf(1).equals(loginUser.getLoginUser().getOwnerYn())
	            || Integer.valueOf(1).equals(loginUser.getLoginUser().getAdminYn());
	    if (!isManager) {
	        return "weple/access-denide";
	    }
	    selectTotalTimeService.removeSelectTotalTime(workId);
	    return "redirect:/totalTimeList";
	}
}