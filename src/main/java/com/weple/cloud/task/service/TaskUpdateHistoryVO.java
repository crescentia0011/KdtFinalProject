package com.weple.cloud.task.service;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

//테이블 값을 받아올 vo
@NoArgsConstructor
@Getter
@Setter
@ToString
public class TaskUpdateHistoryVO {
	private Long historyId;
	private String userName;
	private LocalDateTime actionAt;
	private String actionType;
	private String fieldName;
	private String oldValue;
	private String newValue;
}
