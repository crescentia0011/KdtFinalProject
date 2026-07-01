package com.weple.cloud.gantt.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.weple.cloud.gantt.mapper.GanttMapper;
import com.weple.cloud.gantt.service.GanttResponseDTO;
import com.weple.cloud.gantt.service.GanttService;
import com.weple.cloud.gantt.service.GanttTaskElementVO;
import com.weple.cloud.milestone.mapper.MilestoneMapper;
import com.weple.cloud.milestone.service.MilestoneInfoVO;
import com.weple.cloud.task.mapper.TaskMapper;
import com.weple.cloud.task.service.TaskVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GanttServiceImpl implements GanttService {

    private final MilestoneMapper milestoneMapper;
    private final TaskMapper taskMapper;
    private final GanttMapper ganttMapper;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private String getMilestoneStatusText(String status) {
        if ("g1".equals(status)) return "진행 중";
        if ("g2".equals(status)) return "완료";
        return status;
    }

    private String getTaskStatusText(String status) {
        if ("e1".equals(status)) return "신규";
        if ("e2".equals(status)) return "진행 중";
        if ("e3".equals(status)) return "완료";
        if ("e4".equals(status)) return "결함";
        return status != null ? status : "신규";
    }

    @Override
    public GanttResponseDTO getGanttChartData(Long projectId) {
        List<GanttTaskElementVO> ganttDataList = new ArrayList<>();

        // 1. 기존에 만드신 쿼리로 계층형 마일스톤 데이터 가져오기 (부모-자식 트리구조)
        List<MilestoneInfoVO> parentMilestones = milestoneMapper.selectMilestoneAll(projectId);
        
        // 2. 해당 프로젝트의 전체 일감(Task) 가져오기
        List<TaskVO> allTasks = ganttMapper.selectTaskAll(projectId);
        
        // ★ [추가] 각 하위 마일스톤별 일감의 "가장 빠른 시작일" 추출하기
        Map<Long, LocalDateTime> childMilestoneStartMap = new HashMap<>();
        for (TaskVO task : allTasks) {
            if (task.getMilestoneId() != null && task.getStartDate() != null) {
                childMilestoneStartMap.merge(
                    task.getMilestoneId(), 
                    task.getStartDate().atStartOfDay(), 
                    (oldDate, newDate) -> newDate.isBefore(oldDate) ? newDate : oldDate // 더 빠른 날짜 선택
                );
            }
        }

        // ★ [추가] 하위 마일스톤 시작일을 기반으로 "상위 마일스톤의 가장 빠른 시작일" 추적
        Map<Long, LocalDateTime> parentMilestoneStartMap = new HashMap<>();
        for (MilestoneInfoVO parent : parentMilestones) {
            if (parent.getChildMilestones() != null) {
                for (MilestoneInfoVO child : parent.getChildMilestones()) {
                    LocalDateTime childStart = childMilestoneStartMap.get(child.getMilestoneId());
                    if (childStart != null) {
                        parentMilestoneStartMap.merge(
                            parent.getMilestoneId(),
                            childStart,
                            (oldDate, newDate) -> newDate.isBefore(oldDate) ? newDate : oldDate
                        );
                    }
                }
            }
        }

        // 3. 마일스톤 데이터 평탄화 (Flat List로 변환) 및 ID 접두어 처리
        for (MilestoneInfoVO parent : parentMilestones) {
            
            // 3-A. 최상위 부모 마일스톤 (버전 역할) 가공
            GanttTaskElementVO parentElement = new GanttTaskElementVO();
            parentElement.setId("M_" + parent.getMilestoneId());

            // 💡 [수정] 코드값(g1, g2)을 한글 상태로 치환하여 반영
            String parentStatusName = getMilestoneStatusText(parent.getMilestoneStatus());
            parentElement.setText("[" + parentStatusName + "] " + parent.getMilestoneTitle());
            
            // 💡 [방어 코드] DB에 마감일이 null인 경우 현재 시간으로 안전하게 대체
            LocalDateTime actualParentEnd = (parent.getFinishDate() != null) 
                                            ? parent.getFinishDate().atStartOfDay() 
                                            : LocalDateTime.now();
            
            LocalDateTime parentStart = parentMilestoneStartMap.get(parent.getMilestoneId());
            // 자식 일감의 시작일이 없으면 방어용 종료일을 시작일로 맵핑
            LocalDateTime actualParentStart = (parentStart != null) ? parentStart : actualParentEnd;
            
            // 시작일이 종료일보다 늦어지는 역전 현상 방지
            if (actualParentStart.isAfter(actualParentEnd)) {
                actualParentEnd = actualParentStart;
            }
            
            parentElement.setStart_date(actualParentStart.format(DATE_FORMATTER));
            long parentDuration = ChronoUnit.DAYS.between(actualParentStart, actualParentEnd) + 1;
            parentElement.setDuration((int) parentDuration); 
            
            parentElement.setProgress(parent.getProgressPercentage() / 100.0);
            parentElement.setParent(null); 
            parentElement.setType("task"); 
            ganttDataList.add(parentElement);

            // 3-B. 자식 마일스톤 가공
            if (parent.getChildMilestones() != null) {
                for (MilestoneInfoVO child : parent.getChildMilestones()) {
                    GanttTaskElementVO childElement = new GanttTaskElementVO();
                    // 💡 [수정] 자식 마일스톤도 상태 텍스트 표기 추가
                    childElement.setId("M_" + child.getMilestoneId());
                    String childStatusName = getMilestoneStatusText(child.getMilestoneStatus());
                    childElement.setText("[" + childStatusName + "] " + child.getMilestoneTitle());
                    
                    // 💡 [방어 코드] 자식 마일스톤도 동일하게 null 체크 진행
                    LocalDateTime actualChildEnd = (child.getFinishDate() != null) 
                                                   ? child.getFinishDate().atStartOfDay() 
                                                   : LocalDateTime.now();
                    
                    LocalDateTime childStart = childMilestoneStartMap.get(child.getMilestoneId());
                    LocalDateTime actualChildStart = (childStart != null) ? childStart : actualChildEnd;
                    
                    if (actualChildStart.isAfter(actualChildEnd)) {
                        actualChildEnd = actualChildStart;
                    }
                    
                    childElement.setStart_date(actualChildStart.format(DATE_FORMATTER));
                    long childDuration = ChronoUnit.DAYS.between(actualChildStart, actualChildEnd) + 1;
                    childElement.setDuration((int) childDuration);
                    
                    childElement.setProgress(child.getProgressPercentage() / 100.0);
                    childElement.setParent("M_" + parent.getMilestoneId()); 
                    childElement.setType("task"); 
                    ganttDataList.add(childElement);
                }
            }
        }

        // 4. 일감(Task) 데이터 가공 및 적절한 부모 매핑
        for (TaskVO task : allTasks) {
            GanttTaskElementVO taskElement = new GanttTaskElementVO();
            taskElement.setId("T_" + task.getTaskId());
            
            // 💡 [수정] 일감 상태 표기 추가 (e.g., [진행 중] 일감명)
            // ※ task.getTaskStatus() 부분은 실제 프로젝트 VO 구조에 맞춰 수정해 주세요.
            String taskStatusName = getTaskStatusText(task.getTaskStatus()); 
            taskElement.setText("[" + taskStatusName + "] " + task.getTaskTitle());
            
            // 일감 시작일 세팅
            if (task.getStartDate() != null) {
                taskElement.setStart_date(task.getStartDate().atStartOfDay().format(DATE_FORMATTER));
            }
            
            // 시작일과 마감일 사이의 기간(Duration) 계산
            if (task.getStartDate() != null && task.getFinishDate() != null) {
                long days = ChronoUnit.DAYS.between(task.getStartDate(), task.getFinishDate()) + 1;
                taskElement.setDuration((int) days);
                
            } else {
                taskElement.setDuration(1);
            }
            
            taskElement.setProgress((task.getTaskProgress() != null ? task.getTaskProgress() : 0) / 100.0);
            taskElement.setType("task"); // 일반 일감 타입

            // ★ 계층 구조의 핵심 연동부
            if (task.getParentTaskId() != null && !task.getParentTaskId().isEmpty()) {
                // 상위 일감이 존재하는 경우 -> 부모는 상위 일감
                taskElement.setParent("T_" + task.getParentTaskId());
            } else if (task.getMilestoneId() != null) {
                // 상위 일감이 없고 마일스톤에 속한 경우 -> 부모는 하위 마일스톤
                taskElement.setParent("M_" + task.getMilestoneId());
            } else {
                // 어디에도 속하지 않은 경우 최상위로
                taskElement.setParent(null);
            }

            ganttDataList.add(taskElement);
        }

        // 5. 최종 응답 객체 조립
        GanttResponseDTO response = new GanttResponseDTO();
        response.setData(ganttDataList);
        //response.setLinks(new ArrayList<>()); // 링크 정보는 우선 빈 배열 처리

        return response;
    }
}
