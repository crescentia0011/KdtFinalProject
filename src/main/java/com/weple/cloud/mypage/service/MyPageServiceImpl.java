package com.weple.cloud.mypage.service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.weple.cloud.mypage.mapper.MyPageMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MyPageServiceImpl implements MyPageService {

    // file/ProjectFileController.java 와 동일하게 모듈별 하위 폴더를 분리한다.
    private static final String UPLOAD_DIR = "C:/weple_uploads/profile/";
    // WebMvcConfig의 리소스 핸들러("/uploads/profile/** -> file:C:/weple_uploads/profile/")를 통해 서빙되는 웹 경로
    private static final String PROFILE_IMAGE_URL_PREFIX = "/uploads/profile/";

    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024; // 5MB
    private static final List<String> ALLOWED_EXTENSIONS = List.of("jpg", "jpeg", "png", "gif");

    private final MyPageMapper myPageMapper;

    @Override
    public MyPageVO findMyPage(String userCode) {
        validateUserCode(userCode);
        MyPageVO myPage = myPageMapper.selectMyPage(userCode);
        if (myPage == null) {
            throw new IllegalArgumentException("조회할 사용자를 찾을 수 없습니다.");
        }
        return myPage;
    }

    @Override
    @Transactional
    public void updateNotificationSetting(MyPageNotificationUpdateVO request) {
        validateUserCode(request.getUserCode());

        // 관리-사용자등록(UserManagementServiceImpl.toYn)과 동일하게 hidden+checkbox 조합으로
        // "N,Y" 형태로 바인딩되어도 안전하게 Y/N만 남긴다.
        request.setWebNotificationYn(toYn(request.getWebNotificationYn()));
        request.setEmailNotificationYn(toYn(request.getEmailNotificationYn()));

        if (!NotificationAreaType.isValid(request.getNotificationArea())) {
            request.setNotificationArea(NotificationAreaType.DEFAULT);
        }

        if (myPageMapper.updateNotificationSetting(request) != 1) {
            throw new IllegalStateException("알림 설정을 저장할 수 없습니다.");
        }
    }

    @Override
    public MyPageProfileVO findMyPageProfile(String userCode) {
        validateUserCode(userCode);
        MyPageProfileVO profile = myPageMapper.selectMyPageProfile(userCode);
        if (profile == null) {
            throw new IllegalArgumentException("조회할 사용자를 찾을 수 없습니다.");
        }
        return profile;
    }

    @Override
    @Transactional
    public String updateProfileImage(String userCode, MultipartFile file) {
        validateUserCode(userCode);
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("등록할 사진을 선택해주세요.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("이미지 파일은 5MB 이하만 등록할 수 있습니다.");
        }

        String extension = extractExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("이미지 파일(jpg, jpeg, png, gif)만 등록할 수 있습니다.");
        }

        File dir = new File(UPLOAD_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String savedName = UUID.randomUUID() + "." + extension;
        File dest = new File(UPLOAD_DIR + savedName);
        try {
            file.transferTo(dest);
        } catch (IOException e) {
            throw new IllegalStateException("프로필 사진 저장 중 오류가 발생했습니다.", e);
        }

        String profileImageUrl = PROFILE_IMAGE_URL_PREFIX + savedName;
        if (myPageMapper.updateProfileImage(userCode, profileImageUrl) != 1) {
            throw new IllegalStateException("프로필 사진을 저장할 수 없습니다.");
        }
        return profileImageUrl;
    }

    @Override
    @Transactional
    public void deleteProfileImage(String userCode) {
        validateUserCode(userCode);
        myPageMapper.deleteProfileImage(userCode);
    }

    // 내부 유틸

    private void validateUserCode(String userCode) {
        if (userCode == null || userCode.isBlank()) {
            throw new IllegalArgumentException("사용자 정보가 없습니다.");
        }
    }

    private String extractExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1);
    }

    // 관리-사용자등록(UserManagementServiceImpl.toYn)과 동일한 정규화 로직
    private String toYn(String value) {
        return value != null && value.contains("Y") ? "Y" : "N";
    }
}