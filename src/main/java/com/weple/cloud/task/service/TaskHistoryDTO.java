package com.weple.cloud.task.service;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

//누가 언제 한건지 큰틀과 list로 상세를 받아옴
@Getter
@Setter
@NoArgsConstructor
@ToString
public class TaskHistoryDTO {
	 private Long historyId;
	 private String actionType;
	 private String userName;
	 private LocalDateTime actionAt;
	 private List<TaskHistoryDetailDTO> details;
}
