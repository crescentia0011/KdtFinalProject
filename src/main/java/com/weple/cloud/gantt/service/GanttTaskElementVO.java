package com.weple.cloud.gantt.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@ToString
public class GanttTaskElementVO {
	private String id;           // 예: "M_10" (마일스톤), "T_TASK-001" (일감)
    private String text;         // 마일스톤 제목 또는 일감 제목
    private String start_date;   // "YYYY-MM-DD" 형태의 문자열
    private int duration;        // 기간 (일 단위)
    private double progress;     // 진척도 (0.0 ~ 1.0)
    private String parent;       // 부모의 고유 ID (예: "M_5" 또는 "M_10")
    private String type;         // 차트 UI 모양 결정 ("project", "milestone", "task" 중 선택)
}
