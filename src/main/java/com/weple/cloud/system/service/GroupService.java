package com.weple.cloud.system.service;

import java.util.List;
import java.util.Map;

public interface GroupService {
	// -------------------------------그룹 종류------------------------------
	// 전체조회
	public List<SystemGroupVO> findGroupAll(Long companyId, String keyword);

	// 등록
	public int addGroup(SystemGroupVO systemGroupVO);

	// 수정
	public Map<String, Object> modifyGroup(SystemGroupVO systemGroupVO);

	// 삭제
	public Map<String, Object> removeGroup(int groupId);
}