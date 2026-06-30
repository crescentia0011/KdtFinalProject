package com.weple.cloud.notification.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.weple.cloud.notification.mapper.NotificationMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;

    @Override
    @Transactional
    public void create(String userCode, String alarmTag, String alarmContent, String targetType, String targetId) {
        AlarmVO alarmVO = new AlarmVO();
        alarmVO.setUserCode(userCode);
        alarmVO.setAlarmTag(alarmTag);
        alarmVO.setAlarmContent(alarmContent);
        alarmVO.setTargetType(targetType);
        alarmVO.setTargetId(targetId);

        notificationMapper.insertAlarm(alarmVO);
    }

    @Override
    public List<AlarmVO> findAlarmList(String userCode, String status, int offset, int pageSize) {
        List<AlarmVO> list = notificationMapper.findAlarmList(userCode, status, offset, pageSize);
        applyRelativeTime(list);
        return list;
    }

    @Override
    public int countAlarmList(String userCode, String status) {
        return notificationMapper.countAlarmList(userCode, status);
    }

    @Override
    public List<AlarmVO> findRecentAlarmList(String userCode, int limit) {
        List<AlarmVO> list = notificationMapper.findRecentAlarmList(userCode, limit);
        applyRelativeTime(list);
        return list;
    }

    @Override
    public int countUnread(String userCode) {
        return notificationMapper.countUnread(userCode);
    }

    @Override
    public AlarmVO findById(Long alarmId, String userCode) {
        return notificationMapper.findById(alarmId, userCode);
    }

    @Override
    @Transactional
    public AlarmVO toggleCheck(Long alarmId, String userCode) {
        int updatedRows = notificationMapper.toggleCheckYn(alarmId, userCode);
        if (updatedRows == 0) {
            return null;
        }
        return notificationMapper.findById(alarmId, userCode);
    }

    @Override
    @Transactional
    public void markRead(Long alarmId, String userCode) {
        // 이미 읽은 알림이면 WHERE 절(check_yn = 'N')에 걸려 0건 업데이트 -> 별도 분기 불필요
        notificationMapper.markRead(alarmId, userCode);
    }

    @Override
    @Transactional
    public void readAll(String userCode) {
        notificationMapper.updateAllRead(userCode);
    }

    // 내부 유틸

    private void applyRelativeTime(List<AlarmVO> list) {
        if (list == null) return;
        for (AlarmVO vo : list) {
            vo.setRelativeTime(relativeTime(vo.getAlarmDate()));
        }
    }

    private String relativeTime(LocalDateTime alarmDate) {
        if (alarmDate == null) return "";

        LocalDateTime now = LocalDateTime.now();
        Duration diff = Duration.between(alarmDate, now);

        if (diff.isNegative()) {
            diff = Duration.ZERO;
        }

        long minutes = diff.toMinutes();
        if (minutes < 1) {
            return "방금 전";
        }
        if (minutes < 60) {
            return minutes + "분 전";
        }

        long hours = diff.toHours();
        if (hours < 24) {
            return hours + "시간 전";
        }

        long days = diff.toDays();
        if (days == 1) {
            return "어제";
        }

        return alarmDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
}