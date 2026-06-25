package com.weple.cloud.task.service;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//어떤 값을 바꿨는지 변경이력의 상세정보를 담음
@Getter
@Setter
@NoArgsConstructor
public class TaskHistoryDetailDTO {


    private String fieldName;
    private String oldValue;
    private String newValue;
}