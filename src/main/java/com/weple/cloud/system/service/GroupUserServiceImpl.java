package com.weple.cloud.system.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.weple.cloud.system.mapper.GroupUserMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GroupUserServiceImpl implements GroupUserService{
	
	private final GroupUserMapper groupuserMapper;
	
	// -------------------------------그룹 내 사용자------------------------------
	// 그룹 내 사용자 전체조회
	@Override
	public List<SystemGroupUserVO> findGroupUserAll() {
		return groupuserMapper.selectGroupUserAll();
	}

	// 그룹 내 사용자 등록
	@Override
	public int addGroupUser(SystemGroupUserVO systemGroupUserVO) {
		int result = groupuserMapper.updateGroupUser(systemGroupUserVO);
	    return result;
	}

	// 그룹 내 사용자 수정
	@Override
	public Map<String, Object> modefyGroupUser(SystemGroupUserVO systemGroupUserVO) {
		Map<String, Object> map = new HashMap<>();
		boolean isSuccessed = false;
		int result = groupuserMapper.updateGroupUser(systemGroupUserVO);
		if (result >= 1) {
			isSuccessed = true;
		}
		map.put("result", isSuccessed);
		map.put("target", systemGroupUserVO);
		return map;
	}

	// 그룹 내 사용자 삭제
	@Override
	public int removeGroupUser(String userCode) {
		int result = groupuserMapper.deleteGroupUser(userCode.trim());
		return result;
	}
}
