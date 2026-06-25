package com.weple.cloud.time.service;

import java.util.List;
import java.util.Map;

public interface TimeService {
	// -------------------------------프로젝트 내 소요시간------------------------------
	// 전체조회
	public List<WorkTimeVO> findProjectTimeAll(Long projectId);

	// 등록
	public long addProjectTime(WorkTimeVO workTimeVO);

	// 수정
	public Map<String, Object> modifyProjectTime(WorkTimeVO workTimeVO);

	// 삭제
	public long removeProjectTime(long workId);

	// 일감별 소요시간 누적 합계 (프로시저)
	public void updateTaskSpentHours();
}
