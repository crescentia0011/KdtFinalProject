package com.weple.cloud.system.mapper;

import java.util.List;

import com.weple.cloud.system.service.SystemGroupUserVO;

public interface GroupUserMapper {
	// ---------------------------- 그룹 내 사용자 --------------------------
	// 전체조회
	public List<SystemGroupUserVO> selectGroupUserAll();

	// 등록
	public int insertGroupUser(SystemGroupUserVO systemGroupUserVO);

	// 수정
	public int updateGroupUser(SystemGroupUserVO systemGroupUserVO);

	// 삭제
	public int deleteGroupUser(String userCode);
}
