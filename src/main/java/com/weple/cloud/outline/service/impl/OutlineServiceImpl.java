package com.weple.cloud.outline.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.weple.cloud.milestone.service.TaskGroupStatVO;
import com.weple.cloud.outline.mapper.OutlineMapper;
import com.weple.cloud.outline.service.OutlineService;
import com.weple.cloud.outline.service.ProjectGroupMemberDTO;
import com.weple.cloud.outline.service.ProjectProgressDTO;
import com.weple.cloud.project.service.ProjectVO;

import lombok.RequiredArgsConstructor;

@Service // 스프링 빈 등록 필수
@RequiredArgsConstructor // 생성자 주입
public class OutlineServiceImpl implements OutlineService {

	private final OutlineMapper outlineMapper;
	
	@Override
	public ProjectVO getProjectById(Long projectId) {
	    return outlineMapper.selectProjectById(projectId);
	}
	
	@Override
	public List<ProjectGroupMemberDTO> selectProjectMembersByGroup(Long projectId) {
		return outlineMapper.selectProjectMembersByGroup(projectId);
	}
	
	
	
	
	@Override
    public ProjectProgressDTO getProjectProgress(Long projectId) {
        // 1. DB에서 전체 시간 및 총 진척도 기본 정보 조회
        ProjectProgressDTO progressDto = outlineMapper.selectProjectProgressSummary(projectId);
        
        // 일감이 하나도 없는 경우를 대비한 방어 코드
        if (progressDto == null) {
            progressDto = new ProjectProgressDTO();
        }
        
        // 2. 4대 기준별 통계 리스트 조회 및 백분율 계산 후 각각 올바른 필드에 세팅
        progressDto.setStatusStats(calculatePercentages(outlineMapper.selectTaskStatusStats(projectId)));
        progressDto.setPriorityStats(calculatePercentages(outlineMapper.selectTaskPriorityStats(projectId)));
        progressDto.setTypeStats(calculatePercentages(outlineMapper.selectTaskTypeStats(projectId)));
        progressDto.setManagerStats(calculatePercentages(outlineMapper.selectTaskManagerStats(projectId)));
        
        return progressDto;
    }
    
    // 진척도 백분율(%) 산출 공통 편의 메서드
    private List<TaskGroupStatVO> calculatePercentages(List<TaskGroupStatVO> stats) {
        if (stats == null) return new ArrayList<>();
        
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

}
