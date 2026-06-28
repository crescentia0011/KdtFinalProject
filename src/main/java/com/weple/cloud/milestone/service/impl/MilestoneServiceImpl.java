package com.weple.cloud.milestone.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.weple.cloud.milestone.mapper.MilestoneMapper;
import com.weple.cloud.milestone.service.MilestoneDetailVO;
import com.weple.cloud.milestone.service.MilestoneInfoVO;
import com.weple.cloud.milestone.service.MilestoneService;
import com.weple.cloud.milestone.service.MilestoneVO;
import com.weple.cloud.milestone.service.TaskGroupStatVO;
import com.weple.cloud.system.service.TaskTypeVO;
import com.weple.cloud.task.service.TaskVO;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MilestoneServiceImpl implements MilestoneService {

	private final MilestoneMapper milestoneMapper;
	
	// 마일스톤 전체조회
	@Override
	public List<MilestoneInfoVO> selectMilestoneAll(Long projectId) {
		return milestoneMapper.selectMilestoneAll(projectId);
	}

	// 마일스톤 상세조회
	@Override
    public MilestoneDetailVO getMilestoneDetailInfo(Long projectId, Long milestoneId) {
        // 기본 정보 및 시간 총합 가져오기
        MilestoneDetailVO detailVO = milestoneMapper.selectMilestoneDetailBase(projectId, milestoneId);
        
        if (detailVO == null) {
            throw new IllegalArgumentException("존재하지 않는 마일스톤입니다.");
        }

        // 4대 분류 통계 리스트 세팅
        detailVO.setStatusStats(calculatePercentages(milestoneMapper.selectTaskStatusStats(projectId, milestoneId)));
        detailVO.setPriorityStats(calculatePercentages(milestoneMapper.selectTaskPriorityStats(projectId, milestoneId)));
        detailVO.setTypeStats(calculatePercentages(milestoneMapper.selectTaskTypeStats(projectId, milestoneId)));
        detailVO.setManagerStats(calculatePercentages(milestoneMapper.selectTaskManagerStats(projectId, milestoneId)));

        return detailVO;
    }

	// 마일스톤 상세페이지 연결된 일감 불러오기
    @Override
    public List<TaskVO> getMilestoneTasksWithPaging(Long projectId, Long milestoneId, int page, int pageSize) {
        int startRow = (page - 1) * pageSize + 1;
        int endRow = page * pageSize;
        return milestoneMapper.selectMilestoneTasksWithPaging(projectId, milestoneId, startRow, endRow);
    }

    // 진척도 백분율(%) 산출 공통 편의 메서드
    private List<TaskGroupStatVO> calculatePercentages(List<TaskGroupStatVO> stats) {
        for (TaskGroupStatVO stat : stats) {
            if (stat.getTotalCount() > 0) {
                int percentage = Math.round(((float) stat.getClosedCount() / stat.getTotalCount()) * 100);
                stat.setProgressPercentage(percentage);
            } else {
                stat.setProgressPercentage(0);
            }
        }
        return stats;
    }
	

    // 마일스톤 등록
    @Transactional // [핵심] 두 작업을 하나의 단위로 묶어 무결성 보장
    @Override
    public void addMilestone(MilestoneVO milestoneVO, List<String> taskIds) {
        
        // 1. 마일스톤 데이터 저장 (실행 완료 후 milestoneVO 내부에 새 milestoneId가 자동 주입됨)
        milestoneMapper.insertMilestone(milestoneVO);
        
        // 2. 화면에서 체크하여 넘어온 일감 ID 목록이 존재할 경우에만 벌크 업데이트 실행
        if (taskIds != null && !taskIds.isEmpty()) {
            milestoneMapper.updateTasksMilestoneId(
                milestoneVO.getProjectId(),
                milestoneVO.getMilestoneId(), 
                taskIds
            );
        }
    }
	
	
	
	// 상위 마일스톤 조회
	@Override
	public List<MilestoneVO> getMilestoneListByProjectId(Long projectId) {
	    return milestoneMapper.selectMilestoneListByProjectId(projectId);
	}
	
	@Override
    public List<TaskTypeVO> getTaskTypeList() {
        return milestoneMapper.selectTaskTypeList();
    }

    @Override
    public List<TaskVO> getUnassignedTaskList(Long projectId, int startRow, int endRow, String taskStatus, String priority, String taskManager, Long typeId) {
        return milestoneMapper.selectUnassignedTasks(projectId, startRow, endRow, taskStatus, priority, taskManager, typeId);
    }

    @Override
    public int getUnassignedTaskCount(Long projectId, String taskStatus, String priority, String taskManager, Long typeId) {
        return milestoneMapper.selectUnassignedTasksCount(projectId, taskStatus, priority, taskManager, typeId);
    }

	// 마일스톤 편집
	@Override
	public void updateMilestone(MilestoneVO milestoneVO) {
		milestoneMapper.updateMilestone(milestoneVO);
	}

	// 마일스톤 삭제
	@Override
	public int deleteMilestone(Long milestoneId) {
		return milestoneMapper.deleteMilestone(milestoneId);
	}

}
