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
	public List<WikiPageVO> selectWikiList(@Param("projectId") Long projectId);
	public WikiPageVO selectWikiById(@Param("wikiPageId") String wikiPageId);
	public int insertWiki(WikiPageVO vo);
	public int updateWiki(WikiPageVO vo);
	public int deleteWiki(@Param("wikiPageId") String wikiPageId);
	public List<WikiPageVO> searchWiki(@Param("projectId") Long projectId,
                                @Param("keyword")   String keyword);

    // 변경 이력
	public List<WikiHistoryVO> selectHistoryList(@Param("wikiPageId") String wikiPageId);
	public WikiHistoryVO selectHistoryByVersion(@Param("wikiPageId")  String wikiPageId,
                                               @Param("wikiVersion") Integer wikiVersion);

    // 편집 잠금
    // 잠금 획득: 이미 다른 사람이 잠근 경우 0 반환
	public int lockWiki(@Param("wikiPageId") String wikiPageId,
                        @Param("userCode")   String userCode);
    // 잠금 해제: 본인 잠금만 해제
	public int unlockWiki(@Param("wikiPageId") String wikiPageId,
                          @Param("userCode")   String userCode);
    // 잠금 정보 조회 (배너용: lockUserName, lockedAt)
	public WikiPageVO selectLockInfo(@Param("wikiPageId") String wikiPageId);

    // wiki_relations (연관문서)
    // 위키에 연결된 관계 목록 조회 (제목/URL JOIN 포함)
	public List<WikiRelationVO> selectRelationList(@Param("wikiPageId") String wikiPageId);
    // 관계 추가
	public int insertRelation(WikiRelationVO vo);
    // 관계 단건 삭제
	public int deleteRelation(@Param("wikiRelationId") String wikiRelationId);
    // 위키 삭제 시 관계 전체 삭제
	public int deleteRelationByWikiId(@Param("wikiPageId") String wikiPageId);
    // 해시태그로 일감 검색 (#일감번호 파싱 후 호출)
	public List<WikiRelationVO> searchTaskByHashtag(@Param("projectId") Long   projectId,
                                              @Param("keyword")   String keyword);
    // 해시태그로 위키 검색
	public List<WikiRelationVO> searchWikiByHashtag(@Param("projectId") Long   projectId,
                                              @Param("keyword")   String keyword);
}