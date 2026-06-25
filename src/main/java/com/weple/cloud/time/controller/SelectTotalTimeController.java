package com.weple.cloud.time.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.weple.cloud.time.service.SelectTotalTimeService;
import com.weple.cloud.time.service.SelectTotalTimeVO;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class SelectTotalTimeController {
	// -------------------------------전체 소요시간------------------------------
	private final SelectTotalTimeService selectTotalTimeService;

	// 전체조회
	@GetMapping("totalTimeList")
	public String totalTimeList(Model model) {
		List<SelectTotalTimeVO> list = selectTotalTimeService.findSelectTotalTimeAll();
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

	// 삭제
	@GetMapping("/deleteTotalTime")
	public String deleteWork(@RequestParam("workId") long workId, @RequestParam("projectId") long projectId) {
		long result = selectTotalTimeService.removeSelectTotalTime(workId);
		return "redirect:/totalTimeList?projectId=" + projectId;
	}
}
