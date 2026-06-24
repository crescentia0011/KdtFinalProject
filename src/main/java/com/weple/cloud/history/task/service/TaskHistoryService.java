package com.weple.cloud.history.task.service;

public interface TaskHistoryService {
	void insertHistory(String taskId, String changedBy, String actionType, String oldTitle, String newTitle,
            String oldTypeName, String newTypeName);
}
