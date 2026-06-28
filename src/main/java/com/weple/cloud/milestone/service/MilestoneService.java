package com.weple.cloud.milestone.service;

import java.util.List;

import com.weple.cloud.system.service.TaskTypeVO;
import com.weple.cloud.task.service.TaskVO;

public interface MilestoneService {

	// 전체 조회
	public List<MilestoneInfoVO> selectMilestoneAll(Long projectId);
	
	// 상세 조회
	MilestoneDetailVO getMilestoneDetailInfo(Long projectId, Long milestoneId);

	// 상세페이지 연결된 일감 불러오기
	List<TaskVO> getMilestoneTasksWithPaging(Long projectId, Long milestoneId, int page, int pageSize);
	
	// 등록
	public void addMilestone(MilestoneVO milestoneVO, List<String> taskIds);
	
	// 상위 마일스톤 조회
	List<MilestoneVO> getMilestoneListByProjectId(Long projectId);
	
	// 수정
	public void updateMilestone(MilestoneVO milestoneVO);
	
	// 삭제
	public int deleteMilestone(Long milestoneId);

	
	// 일감 유형 전체 목록 조회 추가
    List<TaskTypeVO> getTaskTypeList();
	
    List<TaskVO> getUnassignedTaskList(Long projectId, int startRow, int endRow, String taskStatus, String priority, String taskManager, Long typeId);
    int getUnassignedTaskCount(Long projectId, String taskStatus, String priority, String taskManager, Long typeId);

	

}
