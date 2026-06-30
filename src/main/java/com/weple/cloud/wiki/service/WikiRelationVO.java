package com.weple.cloud.wiki.service;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class WikiRelationVO {
    private Long   wikiRelationId;  // wiki_relation_id (DB: NUMBER)
    private String wikiPageId;      // wiki_page_id  (출처 위키)
    private String targetType;      // target_type: 'TASK' | 'WIKI'
    private Long   projectId;       // project_id
    private String targetTaskId;    // target_task_id (일감 연결 시)
    private String targetWikiId;    // target_wiki_id (위키 연결 시)

    // 조회용 추가 필드
    private String targetTitle;     // 연결된 문서/일감 제목
    private String targetUrl;       // 프론트 링크용
}