package com.weple.cloud.notification.service;

import java.util.Set;

/**
 * 알림 생성 시 사용하는 alarm_tag / target_type 상수 모음.
 *
 * 다른 도메인(일감/댓글/파일/프로젝트)에서 NotificationService.create(...) 를 호출할 때
 * 문자열을 직접 입력하지 않고 이 클래스의 상수를 사용. (오타 방지)
 *
 * 예)
 *   notificationService.create(
 *       assignedUserCode,
 *       AlarmType.TAG_TASK_ASSIGN,
 *       "\"" + task.getTaskTitle() + "\" 일감이 배정되었습니다.",
 *       AlarmType.TARGET_TASK,
 *       task.getTaskId()
 *   );
 */
public final class AlarmType {

    private AlarmType() {
        // 상수 모음 클래스.
    }

    // alarm_tag (알림 유형)
    public static final String TAG_TASK_ASSIGN = "일감 배정";    
    public static final String TAG_STATUS_CHANGE = "상태 변경";    
    public static final String TAG_COMMENT = "댓글 등록";   
    public static final String TAG_FILE = "첨부파일 등록";  
    public static final String TAG_PROJECT_INVITE = "프로젝트 초대"; 
    
    // 담당자가 아닌 같은 프로젝트의 다른 구성원에게 보내는 브로드캐스트용 태그
    // "전체 알림 수신"
    public static final String TAG_TASK_CREATED = "새 일감 등록";
    public static final String TARGET_TASK    = "TASK";
    public static final String TARGET_PROJECT = "PROJECT";
    
    // "나에게 배정된 일감만 수신" 범위에서 받는 태그
    private static final Set<String> MINE_SCOPE_TAGS = Set.of(
            TAG_TASK_ASSIGN, TAG_STATUS_CHANGE, TAG_COMMENT, TAG_FILE
    );
    
    // "맨션 밑 초대만 수신" 범위에서 받는 태그
    private static final Set<String> MENTION_SCOPE_TAGS = Set.of(
            TAG_PROJECT_INVITE
    );
    
    // all-전체, mine-배정만, mention-맨션/초대만
    public static boolean isEligible(String notificationArea, String alarmTag) {
        if (notificationArea == null) {
            return true;
        }
        switch (notificationArea) {
            case "mine":
                return MINE_SCOPE_TAGS.contains(alarmTag);
            case "mention":
                return MENTION_SCOPE_TAGS.contains(alarmTag);
            case "all":
            default:
                return true;
        }
    }
}