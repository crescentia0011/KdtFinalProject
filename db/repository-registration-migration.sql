-- 알림 기능 구현 전에는 저장소 등록 시 알림 ID를 만들지 않으므로 비어 있을 수 있게 둡니다.
ALTER TABLE repository_setting MODIFY (alarm_id NULL);

-- 공개 저장소 등록 단계에서는 접근 토큰이 없을 수 있습니다.
ALTER TABLE repository_setting MODIFY (access_token NULL);
