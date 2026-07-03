package com.weple.cloud.time.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.weple.cloud.time.service.WorkTimeVO;

public interface TimeMapper {
	// -------------------------------프로젝트 내 소요시간------------------------------
	// 전체조회 (본인이 등록한 건만)
	public List<WorkTimeVO> projectTimeAll(@Param("projectId") Long projectId, @Param("userCode") String userCode);

	// 단건 조회
	public WorkTimeVO projectTimeOne(@Param("workId") long workId);

	// 등록
	public long insertProjectTime(WorkTimeVO workTimeVO);

	// 수정
	public long updateProjectTime(WorkTimeVO workTimeVO);

	// 삭제
	public long deleteProjectTime(long workId);

	// 일감별 소요시간 누적 합계 (프로시저)
	public void updateTaskSpentHours();

	// 선택한 작업분류(taskClassificationId)의 라벨이 "완료"인지 확인용
	public int countCompletedClassification(@Param("workName") long workName);
}