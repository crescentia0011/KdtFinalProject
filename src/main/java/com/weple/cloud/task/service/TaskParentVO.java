package com.weple.cloud.task.service;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class TaskParentVO {
	private String taskId;
	private String taskTitle;
}
