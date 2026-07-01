package com.weple.cloud.notification.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.weple.cloud.notification.service.AlarmVO;
import com.weple.cloud.notification.service.NotificationPreferenceVO;

@Mapper
public interface NotificationMapper {

    /**
     * 알림 1건 생성.
     * 일감 배정/상태 변경/댓글 등록/첨부파일 등록/프로젝트 초대 시점에 각 도메인 서비스에서 호출
     * alarm_id 는 시퀀스(alarm_id_seq) 사용.
     * INSERT 전에 selectKey로 미리 발급되서 alarmVO.alarmId 에 세팅
     */
    public int insertAlarm(AlarmVO alarmVO);
    
    // 알림 생성 전 수신자의 알림 수신 범위/이메일 연동 설정을 조회
    public NotificationPreferenceVO selectNotificationPreference(
    		@Param("userCode") String userCode
    		);
    
    // 알림 목록 - status: all/read/unread
    public List<AlarmVO> findAlarmList(
            @Param("userCode") String userCode,
            @Param("status") String status,
            @Param("offset") int offset,
            @Param("pageSize") int pageSize);

    public int countAlarmList(
            @Param("userCode") String userCode,
            @Param("status") String status);

    // 헤더 드롭다운 - 최근 알림 N건 (상태 필터 없이 최신순)
    public List<AlarmVO> findRecentAlarmList(
            @Param("userCode") String userCode,
            @Param("limit") int limit);

    // 읽지 않은 알림 개수
    public int countUnread(@Param("userCode") String userCode);

    // 단건 조회(본인 소유 검증 포함)
    public AlarmVO findById(
            @Param("alarmId") Long alarmId,
            @Param("userCode") String userCode);

    // 읽음/읽지 않음 토글
    public int toggleCheckYn(
            @Param("alarmId") Long alarmId,
            @Param("userCode") String userCode);

    // 알림 클릭 이동 시 읽음 처리 (읽지 않은 상태일 때만 반영됨)
    public int markRead(
            @Param("alarmId") Long alarmId,
            @Param("userCode") String userCode);

    // 모두 읽음 처리
    public int updateAllRead(@Param("userCode") String userCode);
}