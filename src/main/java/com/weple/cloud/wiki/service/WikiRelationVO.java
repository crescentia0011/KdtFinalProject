package com.weple.cloud.wiki.service;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class WikiRelationVO {
    private String wikiRelationId;
    private String wikiPageId; 
    private String targetType; 
    private Long   projectId; 
    private String targetTaskId; 
    private String targetWikiId;

    // 조회용 추가 필드
    private String targetTitle; 
    private String targetUrl;
}