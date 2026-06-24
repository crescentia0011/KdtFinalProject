package com.weple.cloud.history.task.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TaskHistoryMapper {
	void insertTaskHistory(
			@Param("taskId") String taskId,
            @Param("changedBy") String changedBy,
            @Param("actionType") String actionType);
	
	Long selectLastHistoryId();

	void insertTaskHistoryDetail(
	    @Param("historyId") Long historyId,
	    @Param("fieldName") String fieldName,
	    @Param("oldValue") String oldValue,
	    @Param("newValue") String newValue);
}
