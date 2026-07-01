package com.weple.cloud.calendar.service;

import java.util.List;
import java.util.Map;

public interface CalendarService {
	List<Map<String, Object>> getScheduleList(Map<String, Object> paramMap);
}
