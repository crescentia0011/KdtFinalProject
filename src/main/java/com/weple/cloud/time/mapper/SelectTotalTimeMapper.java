package com.weple.cloud.time.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.weple.cloud.time.service.SelectTotalTimeVO;


public interface SelectTotalTimeMapper {
	// -------------------------------전체 소요시간------------------------------
	// 전체조회 (관리자: 소속 회사 전체 프로젝트의 전체 사용자 건 / 일반 사용자: 본인이 속한 프로젝트에서 본인이 등록한 건만)
	public List<SelectTotalTimeVO> SelectTotalTimeAll(@Param("companyId") Long companyId,
			@Param("userCode") String userCode, @Param("isManager") boolean isManager);
	
	//등록
	public long insertSelectTotalTime(SelectTotalTimeVO selectTotalTimeVO);
	
	// 수정
	public long updateSelectTotalTime(SelectTotalTimeVO selectTotalTimeVO);
	
	// 삭제
	public long deleteSelectTotalTime(@Param("workId") long workId);
}