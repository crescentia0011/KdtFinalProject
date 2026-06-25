package com.weple.cloud.milestone.service;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class MilestoneDetailVO extends MilestoneInfoVO {
    // 시간 추적 통계 데이터
    private double totalEstimatedHours; // 연결된 모든 일감의 추정시간 총합
    private double totalSpentHours;     // 연결된 모든 일감의 소요시간 총합

    // 4대 기준별 통계 리스트
    private List<TaskGroupStatVO> statusStats;   // 상태별 완료 여부
    private List<TaskGroupStatVO> priorityStats; // 우선순위별 완료 여부
    private List<TaskGroupStatVO> typeStats;     // 유형별 완료 여부
    private List<TaskGroupStatVO> managerStats;  // 담당자별 완료 여부
    
}
