package com.weple.cloud.mypage.mapper;

import org.apache.ibatis.annotations.Param;

import com.weple.cloud.mypage.service.MyPageNotificationUpdateVO;
import com.weple.cloud.mypage.service.MyPageProfileVO;
import com.weple.cloud.mypage.service.MyPageVO;

public interface MyPageMapper {

    // 조회: 계정 정보 + 알림 설정 현재값
	public MyPageVO selectMyPage(String userCode);

    // 등록 버튼: 알림 3항목만 저장
	public int updateNotificationSetting(MyPageNotificationUpdateVO request);

    // 조회: 프로필 사진 관리 화면 상단 정보
	public MyPageProfileVO selectMyPageProfile(String userCode);

    // 등록(적용) - 사진 변경 반영
	public int updateProfileImage(@Param("userCode") String userCode, @Param("profileImage") String profileImage);

    // 등록(적용) - 사진 삭제(기본 이미지로) 반영
	public int deleteProfileImage(String userCode);
}