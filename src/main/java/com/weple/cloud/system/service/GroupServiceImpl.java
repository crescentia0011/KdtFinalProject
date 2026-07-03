package com.weple.cloud.system.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.weple.cloud.system.mapper.GroupMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {

	private final GroupMapper groupMapper;

	// -------------------------------그룹 종류------------------------------
	// 그룹 전체조회
	@Override
	public List<SystemGroupVO> findGroupAll(Long companyId, String keyword) {
		return groupMapper.selectGroupAll(companyId, keyword);
	}

	// 그룹 등록
	@Override
	public int addGroup(SystemGroupVO systemGroupVO) {
		// companyId는 컨트롤러에서 로그인 사용자 기준으로 이미 세팅해서 넘어옴
		int result = groupMapper.insertGroup(systemGroupVO);
		return result == 1 ? systemGroupVO.getGroupId() : -1;
	}

	// 그룹 수정
	@Override
	public Map<String, Object> modifyGroup(SystemGroupVO systemGroupVO) {
		Map<String, Object> map = new HashMap<>();
		int result = groupMapper.updateGroup(systemGroupVO);
		if (result >= 1) {
			map.put("groupId", systemGroupVO.getGroupId());
		}
		return map;
	}

	// 그룹 삭제
	@Override
	public Map<String, Object> removeGroup(int groupId) {
		Map<String, Object> map = new HashMap<>();
		int result = groupMapper.deleteGroup(groupId);
		if (result >= 1) {
			map.put("groupId", groupId);
		}
		return map;
	}

}