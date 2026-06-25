package com.weple.cloud.milestone.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.weple.cloud.milestone.service.MilestoneDetailVO;
import com.weple.cloud.milestone.service.MilestoneInfoVO;
import com.weple.cloud.milestone.service.MilestoneVO;
import com.weple.cloud.milestone.service.TaskGroupStatVO;
import com.weple.cloud.task.service.TaskVO;

public interface MilestoneMapper {
	
	// 전체 조회
	public List<MilestoneInfoVO> selectMilestoneAll(@Param("projectId")Long projectId);
	
	// 상세 조회
	// 기본 정보 및 총 추정/소요시간 조회
    MilestoneDetailVO selectMilestoneDetailBase(@Param("projectId")Long projectId, @Param("milestoneId") Long milestoneId);

    // 20개 단위 일감 페이징 리스트 조회
    List<TaskVO> selectMilestoneTasksWithPaging(@Param("projectId")Long projectId, 
    											@Param("milestoneId") Long milestoneId, 
                                                @Param("startRow") int startRow, 
                                                @Param("endRow") int endRow);

    // 4대 기준 통계 리스트 조회 전용 매퍼 메소드들
    List<TaskGroupStatVO> selectTaskStatusStats(@Param("projectId")Long projectId, @Param("milestoneId") Long milestoneId);
    List<TaskGroupStatVO> selectTaskPriorityStats(@Param("projectId")Long projectId, @Param("milestoneId") Long milestoneId);
    List<TaskGroupStatVO> selectTaskTypeStats(@Param("projectId")Long projectId, @Param("milestoneId") Long milestoneId);
    List<TaskGroupStatVO> selectTaskManagerStats(@Param("projectId")Long projectId, @Param("milestoneId") Long milestoneId);
	
	
	// 등록
	public int insertMilestone(MilestoneVO milestoneVO);
	
	// 수정
	public void updateMilestone(MilestoneVO milestoneVO);
	
	// 삭제
	public int deleteMilestone(Long milestoneId);
	
	// 진척도에 따라 마일스톤 상태 자동 업데이트
	public void updateMilestoneStatusByTaskProgress(@Param("milestoneId") Long milestoneId);
	
}
