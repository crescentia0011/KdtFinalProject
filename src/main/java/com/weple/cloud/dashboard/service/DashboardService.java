package com.weple.cloud.dashboard.service;

import java.util.List;

import com.weple.cloud.history.worklog.service.WorkLogVO;
import com.weple.cloud.project.service.ProjectVO;
import com.weple.cloud.task.service.TaskVO;

public interface DashboardService {

	List<TaskVO> getTasksDueWithinAWeek(String userCode);

	List<DashboardProjectDTO> getProjectsForDashboard(String userCode, boolean isManager);
	
	List<WorkLogVO> getRecentActivities(String userCode, String projectId);

	

	


}
