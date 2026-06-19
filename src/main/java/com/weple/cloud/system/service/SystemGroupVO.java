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
public class SystemGroupVO {
	private Integer groupId; //그룹아이디
	private Integer companyId; //기업아이디 FK
	private String groupName; //그룹명
	@DateTimeFormat(pattern = "yyyy-MM-dd") 
	private Date createdAt; //그룹 생성일자
	private int userCount; //그룹 내 사용자수
}
