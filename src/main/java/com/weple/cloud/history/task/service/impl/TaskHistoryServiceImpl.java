package com.weple.cloud.history.task.service.impl;

import org.springframework.stereotype.Service;

import com.weple.cloud.history.task.mapper.TaskHistoryMapper;
import com.weple.cloud.history.task.service.TaskHistoryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskHistoryServiceImpl implements TaskHistoryService {
	private final TaskHistoryMapper taskHistoryMapper;
	
	// 수정 이력 저장
	@Override
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
			) {
		
		// task_history 1건 생성
	    taskHistoryMapper.insertTaskHistory(taskId, changedBy, actionType);
	    Long historyId = taskHistoryMapper.selectLastHistoryId();

	    // 변경된 필드들을 요약해서 1건으로 저장
	    StringBuilder oldSummary = new StringBuilder();
	    StringBuilder newSummary = new StringBuilder();

	    appendIfChanged(oldSummary, newSummary, "제목",     oldTitle,         newTitle);
	    appendIfChanged(oldSummary, newSummary, "설명",     oldTaskDescribe,  newTaskDescribe);
	    appendIfChanged(oldSummary, newSummary, "유형",     oldTypeName,      newTypeName);
	    appendIfChanged(oldSummary, newSummary, "상태",     oldStatus,        newStatus);
	    appendIfChanged(oldSummary, newSummary, "담당자",   oldManager,       newManager);
	    appendIfChanged(oldSummary, newSummary, "우선순위", oldPriority,      newPriority);
	    appendIfChanged(oldSummary, newSummary, "시작일",   oldStartDate,     newStartDate);
	    appendIfChanged(oldSummary, newSummary, "완료일",   oldFinishDate,    newFinishDate);
	    appendIfChanged(oldSummary, newSummary, "추정시간", oldEstimatedTime, newEstimatedTime);
	    appendIfChanged(oldSummary, newSummary, "진척도",   oldProgress,      newProgress);
	    appendIfChanged(oldSummary, newSummary, "상위일감", oldParentTask,    newParentTask);

	    // 변경사항이 있을 때만 detail 1건 저장
	    if (oldSummary.length() > 0 || newSummary.length() > 0) {
	    	
	    	String finalOldValue = oldSummary.toString();
	        String finalNewValue = newSummary.toString();

	        // 💡 오라클 VARCHAR2(255) 칼럼 제한(255자)을 넘지 않도록 안전하게 250자로 커팅
	        if (finalOldValue.length() > 250) {
	            finalOldValue = finalOldValue.substring(0, 247) + "...";
	        }
	        if (finalNewValue.length() > 250) {
	            finalNewValue = finalNewValue.substring(0, 247) + "...";
	        }

	        // 💡 안전하게 가공된 문자열을 파라미터로 전달
	        taskHistoryMapper.insertTaskHistoryDetail(
	            historyId,
	            "summary",
	            finalOldValue,
	            finalNewValue
	        );
	    }
	}
	
	

	// 변경된 필드만 요약 문자열에 추가
	private void appendIfChanged(StringBuilder oldSb, StringBuilder newSb,
	                              String label, String oldVal, String newVal) {
	    String o = oldVal == null ? "" : oldVal.trim();
	    String n = newVal == null ? "" : newVal.trim();
	    if (!o.equals(n)) {
	        if (oldSb.length() > 0) oldSb.append(", ");
	        if (newSb.length() > 0) newSb.append(", ");
	        oldSb.append(label).append(":").append(o);
	        newSb.append(label).append(":").append(n);
	    }
}
	
	
}
