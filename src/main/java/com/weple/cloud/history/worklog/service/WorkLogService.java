package com.weple.cloud.history.worklog.service;

import java.util.List;

public interface WorkLogService {
	public List<WorkLogVO> findAll(String projectId, String startDate, String endDate, String userCode, List<String> typeNames);
}
