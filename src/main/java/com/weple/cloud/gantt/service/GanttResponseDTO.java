package com.weple.cloud.gantt.service;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
@AllArgsConstructor
public class GanttResponseDTO {
    // 마일스톤과 일감이 모두 문자열 ID로 변환되어 이 하나의 리스트에 담깁니다.
    private List<GanttTaskElementVO> data;
    // (선택) 작업 간의 선후 관계 선을 연결할 때 사용 (일단 빈 리스트로 시작해도 무방)
    //private List<GanttLinkElement> links; 
}
