package com.weple.cloud.system.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.weple.cloud.system.service.TaskTypeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class SystemRestController {

    private final TaskTypeService taskTypeService;


    // RESTful 규칙: 행위(Delete)는 HTTP Method로 표현하고, URL에는 자원 명사만 남깁니다.
    @DeleteMapping("/system/taskType/{typeId}")
    public ResponseEntity<String> systemTaskTypeDelete(@PathVariable("typeId") int typeId) {
        taskTypeService.deleteTaskType(typeId);
        return ResponseEntity.ok("SUCCESS");
    }
}
