package com.weple.cloud.testcase.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.DynamicUpdate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "test_case")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicUpdate // 변경된 필드만 Update 쿼리를 날려 최적화
public class TestCase {

	@Id
	@Column(name = "test_id", length = 50)
	private String testId;

	@Column(name = "task_id")
	private String taskId;

	@Column(name = "milestone_version")
	private String milestoneVersion;

	@Column(name = "project_id")
	private Long projectId;

	@Column(name = "user_code")
	private String userCode;

	@Column(name = "test_name")
	private String testName;

	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "test_date")
	private LocalDate testDate;

	@Column(name = "priority")
	private String priority;

	@Column(name = "test_yn", length = 1)
	private String testYn;

	@Column(name = "test_manager")
	private String testManager;

	@Column(name = "test_content")
	private String testContent;

	@Column(name = "test_describe")
	private String testDescribe;

	@Column(name = "coverage_status")
	private String coverageStatus;
	
	@PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

	public void update(String taskId, String milestoneVersion, String testName, LocalDate testDate, String priority,
			String testYn, String testManager, String testContent, String testDescribe, String coverageStatus) {

		if (taskId != null)
			this.taskId = taskId;
		if (milestoneVersion != null)
			this.milestoneVersion = milestoneVersion;
		if (testName != null)
			this.testName = testName;
		if (testDate != null)
			this.testDate = testDate;
		if (priority != null)
			this.priority = priority;
		if (testYn != null)
			this.testYn = testYn;
		if (testManager != null)
			this.testManager = testManager;
		if (testContent != null)
			this.testContent = testContent;
		if (testDescribe != null)
			this.testDescribe = testDescribe;
		if (coverageStatus != null)
			this.coverageStatus = coverageStatus;
	}

}