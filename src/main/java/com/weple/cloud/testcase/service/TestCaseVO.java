package com.weple.cloud.testcase.service;



import java.util.Date;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class TestCaseVO {
	private String testId;      
    private String taskId;      
    private String milestoneVersion; 
    private Long projectId;   
    private String userCode;     
    private String testName;     
    private Date createdAt;      
    private Date testDate;         
    private String priority;       
    private String testYn;       
    private String testManager;
    private String testContent;   
    private String testDescribe;  
    private String coverageStatus;
    
    //조인 조회 값 컬럼
    private String userName;
    private String email;
    private String taskTitle;
    private String managerName;
}
