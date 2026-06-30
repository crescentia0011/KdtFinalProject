package com.weple.cloud.wiki.service;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class WikiPageVO {
    private String wikiPageId;
    private Long projectId;
    private String parentPageId;
    private String title;
    private String content;
    private Integer currentVersion;
    private String userCode;

    // 편집 잠금
    private String lockUserCode;
    private LocalDateTime lockedAt;
    private String lockUserName;   // 추가: 잠금자 이름 (users JOIN)

    private LocalDateTime createdAt;
    private LocalDateTime updateAt;

    // 조회용 추가 필드
    private String userName;
    private String userEmail;
    private String userProfileImg;  // 프로필 이미지 URL
    private List<WikiPageVO> children;
}