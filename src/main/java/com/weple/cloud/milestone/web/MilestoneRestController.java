package com.weple.cloud.milestone.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.weple.cloud.milestone.service.MilestoneService;
import com.weple.cloud.milestone.service.MilestoneVO;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/project/milestone/api")
public class MilestoneRestController {

    private final MilestoneService milestoneService;

    /**
     * 마일스톤 상세페이지 - 연결된 일감 실시간 업데이트
     */
    @PostMapping("/update-task-mapping")
    public ResponseEntity<Map<String, Object>> updateTaskMapping(
            @RequestParam("projectId") Long projectId,
            @RequestParam("milestoneId") Long milestoneId,
            @RequestParam(value = "taskIds", required = false) List<String> taskIds) {
        
        // 브라우저(JS)로 반환할 결과 데이터 바구니 생성
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 1. 서비스 메서드 시그니처에 맞게 MilestoneVO 객체 생성 및 데이터 세팅
            MilestoneVO milestoneVO = new MilestoneVO();
            milestoneVO.setProjectId(projectId);
            milestoneVO.setMilestoneId(milestoneId);
            
            // 2. 작성하신 서비스 로직 호출 (트랜잭션 안에서 1번 쿼리, 2번 쿼리가 순차 실행됨)
            milestoneService.modifyMilestoneTasks(milestoneVO, taskIds);
            
            // 3. 성공 피드백 구성
            response.put("success", true);
            response.put("message", "일감 연결 상태가 성공적으로 반영되었습니다.");
            
            // 200 OK 상태코드와 함께 JSON 반환
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            // 에러 발생 시 콘솔에 로그를 남기고 브라우저에 실패 알림
            e.printStackTrace();
            
            response.put("success", false);
            response.put("message", "일감 연결 중 오류가 발생했습니다: " + e.getMessage());
            
            // 500 Internal Server Error 상태코드와 함께 반환
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
