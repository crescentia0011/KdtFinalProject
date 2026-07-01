package com.weple.cloud.calendar.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.weple.cloud.calendar.mapper.CalendarMapper;
import com.weple.cloud.calendar.service.CalendarService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CalendarServiceImpl implements CalendarService {
	
	private final CalendarMapper calendarMapper;
	
	@Override
    public List<Map<String, Object>> getScheduleList(Map<String, Object> paramMap) {
        

        List<Map<String, Object>> scheduleList = calendarMapper.selectScheduleList(paramMap);


        return applyThemeColors(scheduleList);
    }

    private List<Map<String, Object>> applyThemeColors(List<Map<String, Object>> list) {
        for (Map<String, Object> event : list) {
            String type = (String) event.get("eventType");
            

            String bgColor = "#f3f4f6"; 
            String txtColor = "#374151";

            if ("PROJECT_ALL".equals(type)) {
                bgColor = "#e9d5ff"; txtColor = "#7e22ce";
            } else if ("MY_TASK".equals(type)) {
                bgColor = "#ffedd5"; txtColor = "#ea580c";
            } else if ("MILESTONE".equals(type)) {
                bgColor = "#dbeafe"; txtColor = "#2563eb";
            } else if ("PARENT_TASK".equals(type)) {
                bgColor = "#fce7f3"; txtColor = "#db2777";
            }


            event.put("backgroundColor", bgColor);
            event.put("borderColor", bgColor);
            event.put("textColor", txtColor);
        }
        
        return list;
    }
	

}
