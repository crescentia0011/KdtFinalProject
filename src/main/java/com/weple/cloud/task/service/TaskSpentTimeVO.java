package com.weple.cloud.task.service;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class TaskSpentTimeVO {
	private String userName;
	private String spentContent;
	private int spentHour;
	private LocalDateTime createdAt;
}
