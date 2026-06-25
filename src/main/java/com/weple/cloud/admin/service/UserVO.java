package com.weple.cloud.admin.service;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class UserVO {
	private String userCode;
	private String userName;
	private String loginId;
	private String status;
}
