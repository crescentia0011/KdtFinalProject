package com.weple.cloud.notification.service;

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

    public static final String TARGET_TASK    = "TASK";
    public static final String TARGET_PROJECT = "PROJECT";
}