package com.weple.cloud.project.service;

import java.util.Date;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

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
public class ProjectVO {
	private Long projectId;
	private String projectTitle;
	private String projectDescribe;
	private String projectIdentifier;
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date createdAt;
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date finishDate;
	private String status;
	
	// 설정 페이지 - 활성화된 모듈명 목록
	private List<String> moduleNames;
	
}
