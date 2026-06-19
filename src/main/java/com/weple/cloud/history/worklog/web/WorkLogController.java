package com.weple.cloud.history.worklog.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.weple.cloud.history.worklog.service.WorkLogService;
import com.weple.cloud.history.worklog.service.WorkLogVO;

@Controller
public class WorkLogController {
	private final WorkLogService workLogService;
	
	@Autowired
	public WorkLogController(WorkLogService workLogService) {
		this.workLogService = workLogService;
	}
	
	// 작업내역 조회 : worklogList, history/worklog/list.html
	@GetMapping("/weple/worklog/list")
	public String workLogList(Model model) {
		List<WorkLogVO> list = workLogService.findAll();
		model.addAttribute("workLogList", list);
		
		model.addAttribute("currentMenu", "none");
		return "weple/history/worklog";
	}
}
