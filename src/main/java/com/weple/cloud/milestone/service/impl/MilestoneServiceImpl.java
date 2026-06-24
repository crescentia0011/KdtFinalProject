package com.weple.cloud.milestone.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.weple.cloud.milestone.mapper.MilestoneMapper;
import com.weple.cloud.milestone.service.MilestoneDetailVO;
import com.weple.cloud.milestone.service.MilestoneInfoVO;
import com.weple.cloud.milestone.service.MilestoneService;
import com.weple.cloud.milestone.service.MilestoneVO;
import com.weple.cloud.milestone.service.TaskGroupStatVO;
import com.weple.cloud.task.service.TaskVO;

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
	@Override
	public int addMilestone(MilestoneVO milestoneVO) {
		int result = milestoneMapper.insertMilestone(milestoneVO);
		// 성공 시 1, 실패 시 -1 반환
		return result == 1 ? 1 : -1;
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
