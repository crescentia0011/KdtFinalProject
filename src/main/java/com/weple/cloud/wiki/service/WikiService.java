package com.weple.cloud.wiki.service;

import java.util.List;

public interface WikiService {

    // 위키 기본 CRUD
    List<WikiPageVO>    findWikiTree(Long projectId);
    WikiPageVO          findWikiById(String wikiPageId);
    List<WikiPageVO>    searchWiki(Long projectId, String keyword);
    int                 insertWiki(WikiPageVO vo);
    // 등록 + 연관문서 일괄 저장 (relations: JSON 문자열)
    int                 insertWikiWithRelations(WikiPageVO vo, String relationsJson);
    int                 updateWiki(WikiPageVO vo);
    int                 deleteWiki(String wikiPageId);

    // 변경 이력
    List<WikiHistoryVO> findHistoryList(String wikiPageId);
    WikiHistoryVO       findHistoryByVersion(String wikiPageId, Integer wikiVersion);

    // 편집 잠금
    boolean             lockWiki(String wikiPageId, String userCode);   // 성공 true
    void                unlockWiki(String wikiPageId, String userCode);
    WikiPageVO          getLockInfo(String wikiPageId);                  // 배너용

    // 연관문서 (wiki_relations)
    List<WikiRelationVO> findRelationList(String wikiPageId);
    int                  addRelation(WikiRelationVO vo);
    int                  removeRelation(String wikiRelationId);

    // 해시태그 자동완성 검색
    List<WikiRelationVO> searchByHashtag(Long projectId, String keyword, String type);
}