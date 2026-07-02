package com.weple.cloud.milestone.service;

import java.util.List;

import com.weple.cloud.project.service.ProjectVO;
import com.weple.cloud.system.service.TaskTypeVO;
import com.weple.cloud.task.service.TaskVO;

public interface MilestoneService {

	// 전체 조회
	List<MilestoneInfoVO> getRoadmapList(Long projectId);
	
	// 상세 조회
	MilestoneDetailVO getMilestoneDetailInfo(Long projectId, Long milestoneId);

	// 상세페이지 연결된 일감 불러오기
	List<TaskVO> getMilestoneTasksWithPaging(Long projectId, Long milestoneId, int page, int pageSize);
	
	
	// 프로젝트 정보 가져오기
	public ProjectVO findById(Long projectId);
	
	// 버전 등록
	void addVersion(MilestoneVO milestoneVO);
	
								// 등록 페이지 조회
	// 일감 유형 전체 목록 조회 추가
    List<TaskTypeVO> getTaskTypeList();
    
    // 연결안된 일감 전체조회
 	List<TaskVO> getUnassignedTaskList(Long projectId, Long milestoneId, int startRow, int endRow, String taskStatus, String priority, String taskManager, Long typeId);
     
 	// 연결안된 일감 개수파악
 	int getUnassignedTaskCount(Long projectId, Long milestoneId, String taskStatus, String priority, String taskManager, Long typeId);

 	
 	// 상위 마일스톤 조회
 	List<MilestoneVO> getMilestoneListByProjectId(Long projectId);
    
    
	// 등록
	public void addMilestone(MilestoneVO milestoneVO, List<String> taskIds);
	
	
						// 수정 페이지 조회
    
	
	// 수정할 데이터 조회
	MilestoneVO getMilestoneInfoById(Long milestoneId);
	
	// 연결된 일감 조회
	List<TaskVO> getConnectedTaskList(Long milestoneId);
	
	// 본인이 포함된 마일스톤 리스트(본인이 부모&자식 안되도록)
	List<MilestoneVO> getMilestoneListForUpdate(Long projectId, Long milestoneId);
	
	
	// 하위 마일스톤 수정
	void modifyMilestone(MilestoneVO milestoneVO, List<String> taskIds);
	
	// 상위 마일스톤 수정
	public void updateParentMilestone(MilestoneVO milestoneVO);
	
	// 삭제
	public int deleteMilestone(Long milestoneId);

	void modifyMilestoneTasks(MilestoneVO milestoneVO, List<String> taskIds);

	

	

	
	
	
    
	

}
