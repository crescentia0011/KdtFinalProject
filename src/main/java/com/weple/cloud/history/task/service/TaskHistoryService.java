package com.weple.cloud.history.task.service;

public interface TaskHistoryService {
	
	// 수정 이력 저장 (변경된 필드만 detail에 저장)
	public void insertHistory(
			String taskId,
            String changedBy,
            String actionType,
            String oldTitle,         
            String newTitle,
            String oldTaskDescribe,  
            String newTaskDescribe, 
            String oldTypeName,      
            String newTypeName,
            String oldStatus,        
            String newStatus,
            String oldManager,       
            String newManager,
            String oldPriority,      
            String newPriority,
            String oldStartDate,     
            String newStartDate,
            String oldFinishDate,    
            String newFinishDate,
            String oldEstimatedTime, 
            String newEstimatedTime,
            String oldProgress,      
            String newProgress,
            String oldParentTask,    
            String newParentTask
            );
}
