package com.weple.cloud.project.service;

import java.util.List;

import com.weple.cloud.history.worklog.service.WorkLogVO;

public interface ProjectWorkLogService {
	List<WorkLogVO> findAll(
			String projectId,
            String startDate,
            String endDate,
            String userCode,
            List<String> typeNames);
}
