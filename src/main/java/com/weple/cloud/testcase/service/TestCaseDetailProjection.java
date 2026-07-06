package com.weple.cloud.testcase.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface TestCaseDetailProjection {
    String getTestId();
    String getTaskId();
    String getMilestoneVersion();
    Long getProjectId();
    String getUserCode();
    String getTestName();
    LocalDateTime getCreatedAt();
    LocalDate getTestDate();
    String getPriority();
    String getTestYn();
    String getTestManager();
    String getTestContent();
    String getTestDescribe();
    String getCoverageStatus();
    
    // 조인된 컬럼들
    String getUserName();
    String getTaskTitle();
    String getManagerName();
}