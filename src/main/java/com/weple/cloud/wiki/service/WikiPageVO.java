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
	private String lockUserCode;
	private LocalDateTime lockedAt;
	private String lockUserName;
	private LocalDateTime createdAt;
	private LocalDateTime updateAt;
	
	// 조회용 추가 필드
	private String userName; // 작성자 이름
	private String userEmail; // 작성자 이메일
	private List<WikiPageVO> children; // 하위 페이지 목록
}
