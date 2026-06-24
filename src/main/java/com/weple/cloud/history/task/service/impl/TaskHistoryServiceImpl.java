package com.weple.cloud.history.task.service.impl;

import org.springframework.stereotype.Service;

import com.weple.cloud.history.task.mapper.TaskHistoryMapper;
import com.weple.cloud.history.task.service.TaskHistoryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskHistoryServiceImpl implements TaskHistoryService {
	private final TaskHistoryMapper taskHistoryMapper;
	
	@Override
	public void insertHistory(String taskId, String changedBy, String actionType, String oldTitle, String newTitle,
			String oldTypeName, String newTypeName) {
		taskHistoryMapper.insertTaskHistory(taskId, changedBy, actionType);
	    Long historyId = taskHistoryMapper.selectLastHistoryId();
	    taskHistoryMapper.insertTaskHistoryDetail(historyId, "task_title", oldTitle, newTitle);
	    taskHistoryMapper.insertTaskHistoryDetail(historyId, "type_name", oldTypeName, newTypeName);
		
	}


	
	
}
