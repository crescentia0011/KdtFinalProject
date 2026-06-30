package com.weple.cloud.gantt.service.impl;

import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

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

    @Override
    public GanttResponseDTO getGanttChartData(Long projectId) {
        List<GanttTaskElementVO> ganttDataList = new ArrayList<>();

        // 1. 기존에 만드신 쿼리로 계층형 마일스톤 데이터 가져오기 (부모-자식 트리구조)
        List<MilestoneInfoVO> parentMilestones = milestoneMapper.selectMilestoneAll(projectId);
        
        // 2. 해당 프로젝트의 전체 일감(Task) 가져오기
        List<TaskVO> allTasks = ganttMapper.selectTaskAll(projectId);

        // 3. 마일스톤 데이터 평탄화 (Flat List로 변환) 및 ID 접두어 처리
        for (MilestoneInfoVO parent : parentMilestones) {
            
            // 3-A. 최상위 부모 마일스톤 (버전 역할) 가공
            GanttTaskElementVO parentElement = new GanttTaskElementVO();
            parentElement.setId("M_" + parent.getMilestoneId());
            parentElement.setText("[" + parent.getMilestoneStatus() + "] " + parent.getMilestoneTitle());
            // 마일스톤은 시작일이 없으므로 마감일 당일 혹은 임시 포맷팅 처리
            if (parent.getFinishDate() != null) {
                parentElement.setStart_date(parent.getFinishDate().atStartOfDay().format(DATE_FORMATTER));
            }
            parentElement.setDuration(1); // 대분류 폴더 개념이므로 기본값 세팅 (하위 항목에 의해 자동 조절됨)
            parentElement.setProgress(parent.getProgressPercentage() / 100.0);
            parentElement.setParent(null); // 최상위
            parentElement.setType("project"); // 접고 펼칠 수 있는 프로젝트 타입
            ganttDataList.add(parentElement);

            // 3-B. 자식 마일스톤 가공 (기존에 구현하신 collection 내부 루프)
            if (parent.getChildMilestones() != null) {
                for (MilestoneInfoVO child : parent.getChildMilestones()) {
                    GanttTaskElementVO childElement = new GanttTaskElementVO();
                    childElement.setId("M_" + child.getMilestoneId());
                    childElement.setText(child.getMilestoneTitle());
                    if (child.getFinishDate() != null) {
                        childElement.setStart_date(child.getFinishDate().atStartOfDay().format(DATE_FORMATTER));
                    }
                    childElement.setDuration(1);
                    childElement.setProgress(child.getProgressPercentage() / 100.0);
                    childElement.setParent("M_" + parent.getMilestoneId()); // 부모 마일스톤 ID 지정
                    childElement.setType("project");
                    ganttDataList.add(childElement);
                }
            }
        }

        // 4. 일감(Task) 데이터 가공 및 적절한 부모 매핑
        for (TaskVO task : allTasks) {
            GanttTaskElementVO taskElement = new GanttTaskElementVO();
            taskElement.setId("T_" + task.getTaskId());
            taskElement.setText(task.getTaskTitle());
            
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
