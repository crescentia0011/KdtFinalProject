package com.weple.cloud.milestone.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

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
public class MilestoneVO {
	private Long milestoneId;
	private Long projectId;
	private String  userCode;
	private String  milestoneTitle;
	private String  milestoneDescribe;
	
	@DateTimeFormat(pattern = "yyyy-MM-dd") // 수신용 어노테이션
    @JsonFormat(pattern = "yyyy.MM.dd", timezone = "Asia/Seoul")
	private LocalDate    finishDate;
	
	private String  milestoneStatus;
	
	@JsonFormat(pattern = "yyyy.MM.dd", timezone = "Asia/Seoul")
	private LocalDateTime    createdAt;
	
	@JsonFormat(pattern = "yyyy.MM.dd", timezone = "Asia/Seoul")
	private LocalDateTime    updatedAt;
	
	private int parentMilestoneId;
}
