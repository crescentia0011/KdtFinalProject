package com.weple.cloud.outline.service;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ProjectTimeDTO {
    private Long totalEstimatedTime; // 총 추정 시간
    private Long totalSpentTime;     // 총 소요 시간
}
