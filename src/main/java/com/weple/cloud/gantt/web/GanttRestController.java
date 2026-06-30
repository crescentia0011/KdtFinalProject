package com.weple.cloud.gantt.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.weple.cloud.gantt.service.GanttResponseDTO;
import com.weple.cloud.gantt.service.GanttService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/project/gantt")
public class GanttRestController {

    private final GanttService ganttService;

    // 간트차트용 JSON 데이터 반환 API
    @GetMapping("/data")
    public GanttResponseDTO getGanttData(@RequestParam Long projectId) {
        return ganttService.getGanttChartData(projectId);
    }
}
