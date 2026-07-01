package com.weple.cloud.calendar.mapper;

import java.util.List;
import java.util.Map;

public interface CalendarMapper {
	List<Map<String, Object>> selectScheduleList(Map<String, Object> paramMap);
}
