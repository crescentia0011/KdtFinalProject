package com.weple.cloud.wiki.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.weple.cloud.wiki.mapper.WikiMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WikiServiceImpl implements WikiService {

    private final WikiMapper wikiMapper;

    // 위키 기본

    @Override
    public List<WikiPageVO> findWikiTree(Long projectId) {
        return buildTree(wikiMapper.selectWikiList(projectId));
    }

    @Override
    public WikiPageVO findWikiById(String wikiPageId) {
        return wikiMapper.selectWikiById(wikiPageId);
    }

    @Override
    public List<WikiPageVO> searchWiki(Long projectId, String keyword) {
        return wikiMapper.searchWiki(projectId, keyword);
    }

    @Override
    @Transactional
    public int insertWiki(WikiPageVO vo) {
        return wikiMapper.insertWiki(vo);
    }

    /**
     * 위키 등록 + 연관문서 일괄 저장
     */
    @Override
    @Transactional
    public int insertWikiWithRelations(WikiPageVO vo, String relationsJson) {
        int result = wikiMapper.insertWiki(vo);
        if (relationsJson == null || relationsJson.isBlank() || "[]".equals(relationsJson.trim())) {
            return result;
        }
        try {
            com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
            java.util.List<WikiRelationVO> list = om.readValue(
                relationsJson,
                om.getTypeFactory().constructCollectionType(java.util.List.class, WikiRelationVO.class)
            );
            for (WikiRelationVO rel : list) {
                if (rel == null) continue; // removeChip으로 null 마킹된 항목 스킵
                rel.setWikiPageId(vo.getWikiPageId());
                wikiMapper.insertRelation(rel);
            }
        } catch (Exception e) {
            // JSON 파싱 실패 시 관계 저장 생략 (위키 등록은 유지)
        }
        return result;
    }

    @Override
    @Transactional
    public int updateWiki(WikiPageVO vo) {
        int result = wikiMapper.updateWiki(vo); // 프로시저: 백업 + 버전업 + 업데이트
        wikiMapper.unlockWiki(vo.getWikiPageId(), vo.getUserCode()); // 저장 후 잠금 해제
        return result;
    }

    @Override
    @Transactional
    public int deleteWiki(String wikiPageId) {
        wikiMapper.deleteRelationByWikiId(wikiPageId); // 연관문서 먼저 삭제
        return wikiMapper.deleteWiki(wikiPageId);
    }

    // 변경 이력
    @Override
    public List<WikiHistoryVO> findHistoryList(String wikiPageId) {
        return wikiMapper.selectHistoryList(wikiPageId);
    }

    @Override
    public WikiHistoryVO findHistoryByVersion(String wikiPageId, Integer wikiVersion) {
        return wikiMapper.selectHistoryByVersion(wikiPageId, wikiVersion);
    }

    // 편집 잠금
    @Override
    @Transactional
    public boolean lockWiki(String wikiPageId, String userCode) {
        return wikiMapper.lockWiki(wikiPageId, userCode) > 0;
    }

    @Override
    @Transactional
    public void unlockWiki(String wikiPageId, String userCode) {
        wikiMapper.unlockWiki(wikiPageId, userCode);
    }

    @Override
    public WikiPageVO getLockInfo(String wikiPageId) {
        return wikiMapper.selectLockInfo(wikiPageId);
    }

    // 연관문서
    @Override
    public List<WikiRelationVO> findRelationList(String wikiPageId) {
        return wikiMapper.selectRelationList(wikiPageId);
    }

    @Override
    @Transactional
    public int addRelation(WikiRelationVO vo) {
        return wikiMapper.insertRelation(vo);
    }

    @Override
    @Transactional
    public int removeRelation(String wikiRelationId) {
        return wikiMapper.deleteRelation(wikiRelationId);
    }

    /**
     * 해시태그 자동완성 검색
     * type = "TASK" | "WIKI" | "" (전체)
     */
    @Override
    public List<WikiRelationVO> searchByHashtag(Long projectId, String keyword, String type) {
        if (keyword == null || keyword.isBlank()) return Collections.emptyList();

        List<WikiRelationVO> result = new ArrayList<>();
        if ("TASK".equals(type) || type == null || type.isBlank()) {
            result.addAll(wikiMapper.searchTaskByHashtag(projectId, keyword));
        }
        if ("WIKI".equals(type) || type == null || type.isBlank()) {
            result.addAll(wikiMapper.searchWikiByHashtag(projectId, keyword));
        }
        return result;
    }

    // 트리 변환 
    private List<WikiPageVO> buildTree(List<WikiPageVO> flatList) {
        Map<String, WikiPageVO> map   = new LinkedHashMap<>();
        List<WikiPageVO>        roots = new ArrayList<>();

        for (WikiPageVO vo : flatList) {
            vo.setChildren(new ArrayList<>());
            map.put(vo.getWikiPageId(), vo);
        }
        for (WikiPageVO vo : flatList) {
            String pid = vo.getParentPageId();
            if (pid == null || pid.isBlank() || !map.containsKey(pid)) {
                roots.add(vo);
            } else {
                map.get(pid).getChildren().add(vo);
            }
        }
        return roots;
    }
}