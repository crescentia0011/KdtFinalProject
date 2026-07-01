package com.weple.cloud.task.service;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class TaskTestCaseDTO {
	private String testId;
	private String testName;	
}
