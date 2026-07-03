package com.weple.cloud.calendar.web;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.weple.cloud.auth.service.LoginUserDetails;
import com.weple.cloud.calendar.service.CalendarService;
import com.weple.cloud.project.service.ProjectService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class CalendarController {
	
	private final CalendarService calendarService;
	private final ProjectService projectService;
	
	@GetMapping("/project/calendar")
    public String Calendar(@RequestParam("projectId") Long pId, Model model) {
        model.addAttribute("currentMenu", "calendar");
        model.addAttribute("projectId", pId);
        model.addAttribute("project", projectService.findById(String.valueOf(pId)));
        return "weple/calendar/project";
    }
    
    //  데이터(JSON) 반환을 위해 꼭 필요함!
    @ResponseBody 
    @GetMapping("/scheduleList")
    public List<Map<String, Object>> getScheduleList(
            @RequestParam("projectId") Long projectId, // 💡 포인트 2: 프로젝트 ID 추가 수신
            @RequestParam("start") String start,
            @RequestParam("end") String end,
            @RequestParam(value = "filterTypes", required = false) String filterTypes,
            @AuthenticationPrincipal LoginUserDetails loginUser) {
    	
    	if (filterTypes == null || filterTypes.trim().isEmpty()) {
            return Collections.emptyList(); 
        }

    	String userCode = loginUser.getLoginUser().getUserCode();
        Map<String, Object> paramMap = new HashMap<>();
        
        // 맵에 projectId 추가
        paramMap.put("projectId", projectId); 
        paramMap.put("userCode", userCode);

        // 날짜 파라미터 맵핑 (FullCalendar의 'T' 포함 날짜 포맷에서 날짜만 추출)
        paramMap.put("start", start.substring(0, 10)); 
        paramMap.put("end", end.substring(0, 10));

        // 체크박스 필터 문자열 처리

            List<String> filterList = Arrays.asList(filterTypes.split(","));
            
            if (!filterList.contains("PROJECT_ALL")) {
                paramMap.put("filterList", filterList);
            }
            
        System.out.println("캘린더" + calendarService.getScheduleList(paramMap));

        return calendarService.getScheduleList(paramMap);
    }

}
