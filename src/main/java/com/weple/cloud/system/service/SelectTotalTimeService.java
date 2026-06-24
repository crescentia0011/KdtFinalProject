package com.weple.cloud.system.service;

import java.util.List;
import java.util.Map;

public interface SelectTotalTimeService {
	// -------------------------------전체 소요시간------------------------------
	// 전체조회
	public List<SelectTotalTimeVO> findSelectTotalTimeAll();
	
	// 등록
	public long addSelectTotalTime(SelectTotalTimeVO selectTotalTimeVO);
	
	// 수정
	public Map<String, Object> modifySelectTotalTime(SelectTotalTimeVO selectTotalTimeVO);
	
	// 삭제
	public long removeSelectTotalTime(long workId);
}
