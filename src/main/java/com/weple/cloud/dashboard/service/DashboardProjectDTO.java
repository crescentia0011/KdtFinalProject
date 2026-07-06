package com.weple.cloud.dashboard.service;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DashboardProjectDTO {
	private Long projectId;
    private String projectTitle;
    private String projectDescribe;
    private String projectIdentifier;
    private LocalDateTime createdAt;
    private LocalDateTime finishDate;
    private String status;
    private String moduleNames;

    // 🌟 SQL에서 가공한 데이터를 받기 위한 필수 필드 추가
    private int totalTasks;       // 보유한 일감 개수
    private int completedTasks;   // 완료된 일감 개수
    private int taskProgress;     // 진척도 (%)
}
