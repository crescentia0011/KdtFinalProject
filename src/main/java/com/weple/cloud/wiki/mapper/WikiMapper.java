package com.weple.cloud.wiki.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.weple.cloud.wiki.service.WikiHistoryVO;
import com.weple.cloud.wiki.service.WikiPageVO;
import com.weple.cloud.wiki.service.WikiRelationVO;

@Mapper
public interface WikiMapper {

    // 위키 기본 CRUD
    List<WikiPageVO>    selectWikiList(@Param("projectId") Long projectId);
    WikiPageVO          selectWikiById(@Param("wikiPageId") String wikiPageId);
    int                 insertWiki(WikiPageVO vo);
    int                 updateWiki(WikiPageVO vo);
    int                 deleteWiki(@Param("wikiPageId") String wikiPageId);
    List<WikiPageVO>    searchWiki(@Param("projectId") Long projectId,
                                   @Param("keyword")   String keyword);

    // 변경 이력
    List<WikiHistoryVO> selectHistoryList(@Param("wikiPageId") String wikiPageId);
    WikiHistoryVO       selectHistoryByVersion(@Param("wikiPageId")  String wikiPageId,
                                               @Param("wikiVersion") Integer wikiVersion);

    // 편집 잠금
    int        lockWiki(@Param("wikiPageId") String wikiPageId,
                        @Param("userCode")   String userCode);
    int        unlockWiki(@Param("wikiPageId") String wikiPageId,
                          @Param("userCode")   String userCode);
    WikiPageVO selectLockInfo(@Param("wikiPageId") String wikiPageId);

    // wiki_relations (연관문서)
    List<WikiRelationVO> selectRelationList(@Param("wikiPageId") String wikiPageId);
    int                  insertRelation(WikiRelationVO vo);
    int                  deleteRelation(@Param("wikiRelationId") String wikiRelationId);
    int                  deleteRelationByWikiId(@Param("wikiPageId") String wikiPageId);
    List<WikiRelationVO> searchTaskByHashtag(@Param("projectId") Long   projectId,
                                              @Param("keyword")   String keyword);
    List<WikiRelationVO> searchWikiByHashtag(@Param("projectId") Long   projectId,
                                              @Param("keyword")   String keyword);

    // ★ 유저 프로필 이미지 조회
    String selectUserProfileImg(@Param("userCode") String userCode);
}