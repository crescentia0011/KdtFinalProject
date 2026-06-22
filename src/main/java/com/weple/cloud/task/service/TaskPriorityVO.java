package com.weple.cloud.task.service;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class TaskPriorityVO {
	private Integer taskPriorityId;
	private Integer companyId;
	private String priorityName;
	private String defaultYn;
	private String usingYn;
}
