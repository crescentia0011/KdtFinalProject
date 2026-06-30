package com.weple.cloud.project.service;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class ProjectKanbanVO {
	 private int taskId;
	    private int projectId;
	    private String typeName; // 일감 유형
	    private String defaultDescribe; // 상태명 (신규/진행중/검토중/완료)
	    private String priority; // High/Medium/Low
	    private String taskTitle;
	    private String taskManager; // 담당자 표시용 이름
	    private int taskManagerId; // user_code, 본인 여부 체크용
	    @DateTimeFormat(pattern = "yyyy-MM-dd")
		private Date finishDate;
}
