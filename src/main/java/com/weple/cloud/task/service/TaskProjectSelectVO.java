package com.weple.cloud.task.service;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class TaskProjectSelectVO {
	private Long projectId;
	private String projectTitle; 
}
