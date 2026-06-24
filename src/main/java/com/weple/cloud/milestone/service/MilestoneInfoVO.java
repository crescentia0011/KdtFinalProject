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
    private int totalTaskCount;      // 연결된 총 일감 건수
    private int closedTaskCount;     // 완료된 일감 건수
    private int progressPercentage;  // 진척도 (반올림된 정수형 %)
    private List<TaskVO> recentTasks;// 최근 등록된 순서 최대 5개의 일감 리스트
    private int delayDays;
}
