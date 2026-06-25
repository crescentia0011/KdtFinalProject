package com.weple.cloud.admin.service;

import java.util.List;

public interface UserService {
	// 전체 작업내역 - 활성 사용자 전체 목록
	public List<UserVO> findAllActiveUsers();
	
	// 프로젝트 내 작업내역 - 해당 프로젝트 구성원(사용자) 목록
	public List<UserVO> findUsersByProjectId(String userId);
}
