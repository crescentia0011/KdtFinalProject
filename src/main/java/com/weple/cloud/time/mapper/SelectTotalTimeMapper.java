package com.weple.cloud.time.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.weple.cloud.time.service.SelectTotalTimeVO;


public interface SelectTotalTimeMapper {
	// -------------------------------전체 소요시간------------------------------
	// 전체조회
	public List<SelectTotalTimeVO> SelectTotalTimeAll();
	
	//등록
	public long insertSelectTotalTime(SelectTotalTimeVO selectTotalTimeVO);
	
	// 수정
	public long updateSelectTotalTime(SelectTotalTimeVO selectTotalTimeVO);
	
	// 삭제
	public long deleteSelectTotalTime(@Param("workId") long workId);
}
