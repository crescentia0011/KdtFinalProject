package com.weple.cloud.time.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.weple.cloud.time.mapper.SelectTotalTimeMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SelectTotalTimeServiceImpl implements SelectTotalTimeService {

	private final SelectTotalTimeMapper selectTotalTimeMapper;

	// -------------------------------전체 소요시간------------------------------
	// 전체조회
	@Override
	public List<SelectTotalTimeVO> findSelectTotalTimeAll(Long companyId, String userCode, boolean isManager) {
		return selectTotalTimeMapper.SelectTotalTimeAll(companyId, userCode, isManager);
	}

	// 등록
	@Override
	public long addSelectTotalTime(SelectTotalTimeVO selectTotalTimeVO) {
		long result = selectTotalTimeMapper.insertSelectTotalTime(selectTotalTimeVO);
		return result == 1 ? selectTotalTimeVO.getWorkId() : -1;
	}
	
	// 수정
	@Override
	public Map<String, Object> modifySelectTotalTime(SelectTotalTimeVO selectTotalTimeVO) {
		long updatedCount = selectTotalTimeMapper.updateSelectTotalTime(selectTotalTimeVO);
		Map<String, Object> map = new java.util.HashMap<>();
		if (updatedCount > 0) {
			map.put("status", "success");
			map.put("message", "수정되었습니다.");
		} else {
			map.put("status", "fail");
			map.put("message", "수정할 데이터를 찾을 수 없습니다.");
		}
		return map;
	}

	// 삭제
	@Override
	public long removeSelectTotalTime(long workId) {
		long result = selectTotalTimeMapper.deleteSelectTotalTime(workId);
		return result;
	}

	
}