package com.weple.cloud.history.task.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
public class TaskHistoryVO {
	private Long historyId;
    private String taskId;
    private String changedBy;  
    private String actionType;
}
