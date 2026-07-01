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
        
	     // ==========================================================
	     // 💡 [추가] 하위 일감의 마일스톤 역추적을 위한 사전 맵(Map) 생성
	     // ==========================================================
	     Map<String, Long> taskMilestoneMap = new HashMap<>();
	     for (TaskVO task : allTasks) {
	         if (task.getMilestoneId() != null) {
	             // 일감 ID를 키로, 마일스톤 ID를 밸류로 저장
	             taskMilestoneMap.put(task.getTaskId(), task.getMilestoneId()); 
	         }
	     }

     // ==========================================================
        // 💡 [핵심 수정 1] 시작일 기준 정렬 (계단식 배열의 기초)
        // ==========================================================
        
        // A. 상위 마일스톤(버전) 정렬
        parentMilestones.sort((p1, p2) -> {
            LocalDateTime t1 = parentMilestoneStartMap.get(p1.getMilestoneId());
            LocalDateTime t2 = parentMilestoneStartMap.get(p2.getMilestoneId());
            if (t1 == null) t1 = (p1.getFinishDate() != null) ? p1.getFinishDate().atStartOfDay() : LocalDateTime.now();
            if (t2 == null) t2 = (p2.getFinishDate() != null) ? p2.getFinishDate().atStartOfDay() : LocalDateTime.now();
            return t1.compareTo(t2); // 오름차순 (빠른 날짜가 위로)
        });

        // B. 하위 마일스톤 정렬
        for (MilestoneInfoVO parent : parentMilestones) {
            if (parent.getChildMilestones() != null) {
                parent.getChildMilestones().sort((c1, c2) -> {
                    LocalDateTime t1 = childMilestoneStartMap.get(c1.getMilestoneId());
                    LocalDateTime t2 = childMilestoneStartMap.get(c2.getMilestoneId());
                    if (t1 == null) t1 = (c1.getFinishDate() != null) ? c1.getFinishDate().atStartOfDay() : LocalDateTime.now();
                    if (t2 == null) t2 = (c2.getFinishDate() != null) ? c2.getFinishDate().atStartOfDay() : LocalDateTime.now();
                    return t1.compareTo(t2);
                });
            }
        }

        // C. 전체 일감(Task)을 시작일 순으로 먼저 정렬
        allTasks.sort((t1, t2) -> {
            LocalDateTime d1 = t1.getStartDate() != null ? t1.getStartDate().atStartOfDay() : LocalDateTime.MAX;
            LocalDateTime d2 = t2.getStartDate() != null ? t2.getStartDate().atStartOfDay() : LocalDateTime.MAX;
            return d1.compareTo(d2);
        });

        // ==========================================================
        // 💡 [핵심 수정 2] 일감 구조화 및 부모별 그룹핑
        // ==========================================================
        Map<String, List<GanttTaskElementVO>> childrenTasksMap = new HashMap<>();
        List<GanttTaskElementVO> rootTasks = new ArrayList<>(); // 마일스톤도, 상위일감도 없는 독립 일감용

        for (TaskVO task : allTasks) {
            GanttTaskElementVO taskElement = new GanttTaskElementVO();
            taskElement.setId("T_" + task.getTaskId());
            
            String taskStatusName = getTaskStatusText(task.getTaskStatus()); 
            taskElement.setText("[" + taskStatusName + "] " + task.getTaskTitle());
            
            if (task.getStartDate() != null) {
                taskElement.setStart_date(task.getStartDate().atStartOfDay().format(DATE_FORMATTER));
            }
            if (task.getStartDate() != null && task.getFinishDate() != null) {
                long days = ChronoUnit.DAYS.between(task.getStartDate(), task.getFinishDate()) + 1;
                taskElement.setDuration((int) days);
            } else {
                taskElement.setDuration(1);
            }
            taskElement.setProgress((task.getTaskProgress() != null ? task.getTaskProgress() : 0) / 100.0);
            taskElement.setType("task");

            // 💡 [핵심 수정] 일감의 계층을 무시하고 무조건 마일스톤 직속으로 부모를 설정합니다.
            Long targetMilestoneId = task.getMilestoneId();
            
            // 만약 하위 일감이라 본인 로우에 milestoneId가 없다면, 상위 일감의 milestoneId를 상단 맵에서 찾아옵니다.
            if (targetMilestoneId == null && task.getParentTaskId() != null && !task.getParentTaskId().isEmpty()) {
                targetMilestoneId = taskMilestoneMap.get(task.getParentTaskId());
            }

            // 최종 부모 ID 결정 (무조건 M_ 접두어를 붙여 마일스톤 밑으로 강제 배정)
            String parentId = null;
            if (targetMilestoneId != null) {
                parentId = "M_" + targetMilestoneId;
            }
            taskElement.setParent(parentId);

            // 그룹 맵에 바인딩 (이제 T_ 밑이 아니라 M_ 밑으로 쌓이게 됩니다)
            if (parentId == null) {
                rootTasks.add(taskElement);
            } else {
                childrenTasksMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(taskElement);
            }
        }

        // ==========================================================
        // 💡 [핵심 수정 3] 계층 역순 추적 트리 조립 (Pre-order 순서 배치)
        // ==========================================================
        for (MilestoneInfoVO parent : parentMilestones) {
            GanttTaskElementVO parentElement = new GanttTaskElementVO();
            parentElement.setId("M_" + parent.getMilestoneId());
            
            String parentStatusName = getMilestoneStatusText(parent.getMilestoneStatus());
            parentElement.setText("[" + parentStatusName + "] " + parent.getMilestoneTitle());
            
            // 1) 기준 종료일 설정
            LocalDateTime actualParentEnd = (parent.getFinishDate() != null) 
                                            ? parent.getFinishDate().atStartOfDay() 
                                            : LocalDateTime.now();
            
            // 2) 자식들로부터 계산된 시작일 가져오기
            LocalDateTime parentStart = parentMilestoneStartMap.get(parent.getMilestoneId());
            LocalDateTime actualParentStart = (parentStart != null) ? parentStart : actualParentEnd;
            
            // 💡 [핵심 변경] 자식 시작일이 본인 종료일을 넘어섰다면, 시작일을 종료일과 같게 제한
            if (actualParentStart.isAfter(actualParentEnd)) {
                actualParentStart = actualParentEnd; 
            }
            
            parentElement.setStart_date(actualParentStart.format(DATE_FORMATTER));
            long parentDuration = ChronoUnit.DAYS.between(actualParentStart, actualParentEnd) + 1;
            parentElement.setDuration((int) parentDuration); 
            
            parentElement.setProgress(parent.getProgressPercentage() / 100.0);
            parentElement.setParent(null); 
            parentElement.setType("task"); 
            ganttDataList.add(parentElement);
            
            // 버전에 직속된 일감 처리
            appendTasksRecursively("M_" + parent.getMilestoneId(), childrenTasksMap, ganttDataList);

            // ==========================================================
            // 💡 [수정] 3-B. 자식 마일스톤 가공
            // ==========================================================
            if (parent.getChildMilestones() != null) {
                for (MilestoneInfoVO child : parent.getChildMilestones()) {
                    GanttTaskElementVO childElement = new GanttTaskElementVO();
                    childElement.setId("M_" + child.getMilestoneId());
                    
                    String childStatusName = getMilestoneStatusText(child.getMilestoneStatus());
                    childElement.setText("[" + childStatusName + "] " + child.getMilestoneTitle());
                    
                    // 1) 기준 종료일 설정
                    LocalDateTime actualChildEnd = (child.getFinishDate() != null) 
                                                   ? child.getFinishDate().atStartOfDay() 
                                                   : LocalDateTime.now();
                    
                    // 2) 소속 일감들로부터 계산된 가장 빠른 시작일 가져오기
                    LocalDateTime childStart = childMilestoneStartMap.get(child.getMilestoneId());
                    LocalDateTime actualChildStart = (childStart != null) ? childStart : actualChildEnd;
                    
                    // 💡 [핵심 변경] 자식 일감들의 시작일이 마일스톤 종료일을 넘어섰다면, 시작일을 종료일과 같게 제한
                    if (actualChildStart.isAfter(actualChildEnd)) {
                        actualChildStart = actualChildEnd;
                    }
                    
                    childElement.setStart_date(actualChildStart.format(DATE_FORMATTER));
                    long childDuration = ChronoUnit.DAYS.between(actualChildStart, actualChildEnd) + 1;
                    childElement.setDuration((int) childDuration);
                    
                    childElement.setProgress(child.getProgressPercentage() / 100.0);
                    childElement.setParent("M_" + parent.getMilestoneId()); 
                    childElement.setType("task"); 
                    ganttDataList.add(childElement);
                    
                    // 마일스톤에 속한 일감들 연동
                    appendTasksRecursively("M_" + child.getMilestoneId(), childrenTasksMap, ganttDataList);
                }
            }
        }

        // 완전히 독립된 루트 일감 처리
        for (GanttTaskElementVO rootTask : rootTasks) {
            ganttDataList.add(rootTask);
            appendTasksRecursively(rootTask.getId(), childrenTasksMap, ganttDataList);
        }

        GanttResponseDTO response = new GanttResponseDTO();
        response.setData(ganttDataList);
        return response;
    }
    
    /**
     * 💡 [추가] 특정 부모 요소(마일스톤 또는 일감)에 소속된 일감들을 
     * 계단식 순서대로 재귀 호출하며 평탄화 리스트에 끼워 넣는 헬퍼 메서드
     */
    private void appendTasksRecursively(String parentId, Map<String, List<GanttTaskElementVO>> childrenTasksMap, List<GanttTaskElementVO> ganttDataList) {
        List<GanttTaskElementVO> tasks = childrenTasksMap.get(parentId);
        if (tasks != null) {
            for (GanttTaskElementVO task : tasks) {
                ganttDataList.add(task);
                // 만약 이 일감이 또 다른 서브 일감(Sub-task)을 품고 있다면 재귀 호출하여 바로 아래에 계단식으로 장착
                appendTasksRecursively(task.getId(), childrenTasksMap, ganttDataList);
            }
        }
    }
}
