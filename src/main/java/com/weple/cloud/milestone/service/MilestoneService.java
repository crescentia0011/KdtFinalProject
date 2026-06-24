package com.weple.cloud.milestone.service;

import java.util.List;

import com.weple.cloud.milestone.service.MilestoneDetailVO;
import com.weple.cloud.task.service.TaskVO;

public interface MilestoneService {

	// 전체 조회
	public List<MilestoneInfoVO> selectMilestoneAll(Long projectId);
	
	// 상세 조회
	MilestoneDetailVO getMilestoneDetailInfo(Long projectId, Long milestoneId);

	// 상세페이지 연결된 일감 불러오기
	List<TaskVO> getMilestoneTasksWithPaging(Long projectId, Long milestoneId, int page, int pageSize);
	
	// 등록
	public int addMilestone(MilestoneVO milestoneVO);
	
	// 수정
	public void updateMilestone(MilestoneVO milestoneVO);
	
	// 삭제
	public int deleteMilestone(Long milestoneId);

}
