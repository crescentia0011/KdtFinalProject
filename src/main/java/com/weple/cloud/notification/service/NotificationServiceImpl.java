package com.weple.cloud.notification.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.weple.cloud.notification.mapper.NotificationMapper;
import com.weple.cloud.notification.service.mail.NotificationMailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;
    // 메일 발송 기능은 선택사항
    private final ObjectProvider<NotificationMailService> mailServiceProvider;

    @Override
    @Transactional
    public void create(String userCode, String alarmTag, String alarmContent, String targetType, String targetId) {
        
    	// 알림 만들기 전 수신자의 "알림 수신 범위" 설정 확인
    	NotificationPreferenceVO preference = notificationMapper.selectNotificationPreference(userCode);
        if (preference == null) {
        	// 대상 사용자를 찾을 수 없는 경우 알림 만들지 않음
        	log.debug("알림 대상 사용자를 찾을 수 없어 생성을 건너뜁니다. userCode={}", userCode);
            return;
        }
        
        if (!AlarmType.isEligible(preference.getNotificationArea(), alarmTag)) {
            // 예) notificationArea=mine 인 사용자에게 TAG_PROJECT_INVITE/TAG_TASK_CREATED는 만들지 않음
            log.debug("수신 범위({}) 설정으로 알림({})을 건너뜁니다. userCode={}",
                    preference.getNotificationArea(), alarmTag, userCode);
            return;
        }
    	
    	AlarmVO alarmVO = new AlarmVO();
        alarmVO.setUserCode(userCode);
        alarmVO.setAlarmTag(alarmTag);
        alarmVO.setAlarmContent(alarmContent);
        alarmVO.setTargetType(targetType);
        alarmVO.setTargetId(targetId);

        notificationMapper.insertAlarm(alarmVO);
        
        // "이메일 알림 수신 연동"이 켜져 있으면 이메일도 함께 발송
        // 메일 발송 실패가 알림(웹) 생성 자체를 막으면 안 되므로 별도로 try/catch 함
        if ("Y".equals(preference.getEmailNotificationYn())) {
            NotificationMailService mailService = mailServiceProvider.getIfAvailable();
            if (mailService == null) {
                log.debug("메일 발송 서비스가 등록되어 있지 않아 이메일 발송을 건너뜁니다. (spring.mail.* 설정 필요) userCode={}", userCode);
            } else {
                try {
                    mailService.sendAlarmMail(preference.getEmail(), alarmTag, alarmContent);
                } catch (Exception e) {
                    log.warn("알림 메일 발송에 실패했습니다. userCode={}, error={}", userCode, e.getMessage());
                }
            }
        }
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