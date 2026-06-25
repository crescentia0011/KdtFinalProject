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
public class RoleVO {
	private Long roleId;
	private Long companyId;
	private String roleName;
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date createdAt;
	
	// 권한 목록(역할에 매핑된 권한)
	private List<String> permissionCodes;
}
