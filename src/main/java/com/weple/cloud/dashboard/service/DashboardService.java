package com.weple.cloud.dashboard.service;

import java.util.List;

import com.weple.cloud.task.service.TaskVO;

public interface DashboardService {

	List<TaskVO> getTasksDueWithinAWeek(String userCode);

	List<DashboardProjectDTO> getProjectsForDashboard(String userCode, boolean isManager);
	
	List<WorkLog2VO> getRecentActivities(com.weple.cloud.auth.service.LoginUserVO loginUser, String projectId, int limit);
	

	

	


}
