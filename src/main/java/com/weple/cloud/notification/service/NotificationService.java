package com.weple.cloud.notification.service;

import java.util.List;

public interface NotificationService {

    public void create(String userCode, String alarmTag, String alarmContent, String targetType, String targetId);

    // 알림 목록 페이지 - 상태 필터(all/read/unread) + 페이징
    public List<AlarmVO> findAlarmList(String userCode, String status, int offset, int pageSize);
    public int countAlarmList(String userCode, String status);

    // 헤더 드롭다운 - 최근 알림 N건
    public List<AlarmVO> findRecentAlarmList(String userCode, int limit);

    // 헤더 뱃지 - 읽지 않은 알림 개수
    public int countUnread(String userCode);

    // 단건 조회 (소유자 검증 포함)
    public AlarmVO findById(Long alarmId, String userCode);

    // 읽음/읽지 않음 상태 토글
    public AlarmVO toggleCheck(Long alarmId, String userCode);

    // 알림 클릭(이동) 시 읽음 처리. 이미 읽은 알림이면 아무 동작 없음.
    public void markRead(Long alarmId, String userCode);

    // 모두 읽음 처리
    public void readAll(String userCode);
}