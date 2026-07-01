package com.weple.cloud.mypage.service;

import org.springframework.web.multipart.MultipartFile;

public interface MyPageService {

    // 조회
    MyPageVO findMyPage(String userCode);

    // 등록 버튼 - 알림 설정 저장
    void updateNotificationSetting(MyPageNotificationUpdateVO request);

    // 조회
    MyPageProfileVO findMyPageProfile(String userCode);

    // 등록(적용) - 새 사진으로 교체. 반환값은 저장된 웹 경로(예: /uploads/profile/xxx.png)로
    // 컨트롤러에서 헤더 등 화면에 "즉시 갱신"되도록 세션의 LoginUserVO.profileImage도 함께 갱신하는 데 사용
    String updateProfileImage(String userCode, MultipartFile file);

    // 등록(적용) - 기본 이미지로 삭제
    void deleteProfileImage(String userCode);
}