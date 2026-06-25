package com.weple.cloud.system.service;

import java.util.List;
import java.util.Map;

public interface GroupUserService {
	// ---------------------------- 그룹 내 사용자 --------------------------
	// 전체조회
	public List<SystemGroupUserVO> findGroupUserAll();

	// 등록
	public int addGroupUser(SystemGroupUserVO systemGroupUserVO);

	// 수정
	public Map<String, Object> modefyGroupUser(SystemGroupUserVO systemGroupUserVO);

	// 삭제
	public int removeGroupUser(String userCode);
}
