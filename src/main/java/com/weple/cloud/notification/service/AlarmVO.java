package com.weple.cloud.notification.service;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AlarmVO {
	private Long alarmId;
	private String userCode;
	private String alarmTag; // 알림 유형(일감 배정, 상태 변경, 댓글 등록, 첨부파일 등록, 프로젝트 초대)
	private String alarmContent;
	private String checkYn;
	private LocalDateTime alarmDate;
	private String targetType;
	private String targetId;
	private LocalDateTime alarmChk;
	
	private String redirectUrl; // 알림 클릭 시 이동할 화면 경로
	private String contextTitle; // 알림이 발생한 프로젝트명
	private String relativeTime; // 5분 전, 어제 등 상대 시간 표기
}
