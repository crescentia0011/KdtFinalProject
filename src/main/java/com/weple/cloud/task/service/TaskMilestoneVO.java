package com.weple.cloud.task.service;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@ToString
public class TaskMilestoneVO {
	private Long milestoneId;
	private Long projectId;
	private String milestoneTitle;
}
