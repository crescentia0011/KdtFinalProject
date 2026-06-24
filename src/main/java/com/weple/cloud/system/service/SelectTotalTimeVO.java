package com.weple.cloud.system.service;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class SelectTotalTimeVO {
	private long workId;
	private long projectId;
	private String taskId;
	private String userCode;
	// DB work_name 타입 : NUMBER
	private long workName;
	//조인해서 가져온 실제 명칭 (String으로 변경)
	private String workNameLabel;
	@DateTimeFormat(pattern = "yyyy-MM-dd") 
	private Date workDate;
	private String spentContent;
	private long spentHour;
	@DateTimeFormat(pattern = "yyyy-MM-dd") 
	private Date createdAt;
	@DateTimeFormat(pattern = "yyyy-MM-dd") 
	private Date updatedAt;
}