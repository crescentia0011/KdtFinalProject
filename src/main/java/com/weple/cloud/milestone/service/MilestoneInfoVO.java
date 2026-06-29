package com.weple.cloud.milestone.service;

import java.util.List;

import com.weple.cloud.task.service.TaskVO;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter 
@Setter 
@ToString(callSuper = true)
public class MilestoneInfoVO extends MilestoneVO {
	// 상위 마일스톤용 통계 필드
    private int totalMilestoneCount;    // 하위 마일스톤 총 개수
    private int closedMilestoneCount;   // 완료된 하위 마일스톤 개수
    
    // 공통 통계 필드 (상위: 하위 마일스톤들 기준 / 하위: 연결된 일감들 기준)
    private int progressPercentage;     // 평균 진척도 (%)
    private int delayDays;              // 지연 일수
    
    // 하위 마일스톤용 통계 필드
    private int totalTaskCount;         // 연결된 총 일감 개수
    private int closedTaskCount;        // 완료된 일감 개수

    // 하위 마일스톤 리스트
    private List<MilestoneInfoVO> childMilestones;
}
