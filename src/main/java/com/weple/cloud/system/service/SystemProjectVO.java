package com.weple.cloud.system.service;

import java.util.Date;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
@AllArgsConstructor
public class SystemProjectVO {
	private Long projectId;
	private String projectTitle;
	private String projectDescribe;
	private String projectIdentifier;
	private String status;
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date startDate;
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date finishDate;
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date createdAt;
	
	private List<String> moduleNames;
	
	// 검색/페이징
	private String keyword;
    private int page;
    private int pageSize;
    private int offset;
    private int totalCount;
    // 관리자가 아닌 사용자의 프로젝트 목록 필터링용 (본인이 속한 프로젝트만 조회)
    private String userCode;
    private String companyId;
}
