package com.weple.cloud.history.worklog.service;

import java.time.LocalDateTime;

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
public class WorkLogVO {
	private Long historyId;
	private String taskId;
	private String actionType;
	private String changedBy;
	private LocalDateTime actionAt;
	private String defaultDescribe;
	
	private String projectTitle; // 프로젝트명(project 테이블)
	private String userName; // 사용자명(user 테이블)
	private String taskTitle; // 일감명(task 테이블)
	private String taskStatus; // 일감상태(common_code 테이블)
	private String typeName; // 일감유형(task_types 테이블)
	private Double spentHour; // 작업시간(work_time 테이블)
	
}
