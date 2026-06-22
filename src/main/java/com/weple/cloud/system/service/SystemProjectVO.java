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
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date startDate;
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date finishDate;
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date createdAt;
	
	private List<String> moduleNames;
}
