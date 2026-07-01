package com.weple.cloud.mypage.service;

public final class NotificationAreaType {

    private NotificationAreaType() {
        // 상수 모음 클래스.
    }

    public static final String ALL = "all";
    public static final String MINE = "mine";
    public static final String MENTION = "mention";

    // 화면에 표시할 라벨
    public static final String LABEL_ALL = "전체 알림 수신";
    public static final String LABEL_ALL_DESC = "(배정 알림, 참여 알림, 프로젝트 초대 전체)";
    public static final String LABEL_MINE = "나에게 배정된 일감만 수신";
    public static final String LABEL_MINE_DESC = "(본인이 담당자로 지정된 일감 변경 건만)";
    public static final String LABEL_MENTION = "초대만 수신";
    public static final String LABEL_MENTION_DESC = "(프로젝트 참여 초대만)";

    // 저장 값이 비어있거나 잘못된 코드일 때 사용할 기본값
    public static final String DEFAULT = ALL;

    public static boolean isValid(String value) {
        return ALL.equals(value) || MINE.equals(value) || MENTION.equals(value);
    }
}