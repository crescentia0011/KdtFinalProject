package com.weple.cloud.milestone.service;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class TaskGroupStatVO {
    private String groupName;         // 화면에 표시될 명칭 (예: '진행 중', '높음', 'Bug', '홍길동')
    private int totalCount;           // 해당 그룹의 전체 일감 수
    private int closedCount;          // 해당 그룹의 완료(e3)된 일감 수
    private int progressPercentage;   // 해당 그룹의 진척도 (%)
}
