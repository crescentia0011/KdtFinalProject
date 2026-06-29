package com.weple.cloud.wiki.web;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.weple.cloud.auth.service.LoginUserDetails;
import com.weple.cloud.project.service.ProjectService;
import com.weple.cloud.wiki.service.WikiHistoryVO;
import com.weple.cloud.wiki.service.WikiPageVO;
import com.weple.cloud.wiki.service.WikiRelationVO;
import com.weple.cloud.wiki.service.WikiService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class WikiController {

    private final WikiService    wikiService;
    private final ProjectService projectService;

    //  공통 model 세팅
    private void setCommonModel(Model model, Long projectId) {
        model.addAttribute("project",     projectService.findById(String.valueOf(projectId)));
        model.addAttribute("moduleNames", projectService.findActiveModuleNames(projectId));
        model.addAttribute("sidebarMenu", "project");
        model.addAttribute("currentMenu", "wiki");
        model.addAttribute("projectId",   projectId);
    }

    //  위키 조회 페이지
    @GetMapping("/project/wiki")
    public String wikiPage(
            @RequestParam Long projectId,
            @RequestParam(required = false) String  wikiPageId,
            @RequestParam(required = false) String  keyword,
            Model model) {

        List<WikiPageVO> wikiTree = wikiService.findWikiTree(projectId);

        WikiPageVO          selectedWiki = null;
        List<WikiHistoryVO> historyList  = null;
        List<WikiRelationVO> relationList = null;

        // 선택된 위키 결정
        if (wikiPageId != null && !wikiPageId.isBlank()) {
            selectedWiki = wikiService.findWikiById(wikiPageId);
        } else if (!wikiTree.isEmpty()) {
            selectedWiki = wikiService.findWikiById(wikiTree.get(0).getWikiPageId());
        }

        if (selectedWiki != null) {
            historyList  = wikiService.findHistoryList(selectedWiki.getWikiPageId());
            relationList = wikiService.findRelationList(selectedWiki.getWikiPageId());
        }

        // 키워드 검색
        List<WikiPageVO> searchResult = null;
        if (keyword != null && !keyword.isBlank()) {
            searchResult = wikiService.searchWiki(projectId, keyword);
        }

        setCommonModel(model, projectId);
        model.addAttribute("wikiTree",     wikiTree);
        model.addAttribute("selectedWiki", selectedWiki);
        model.addAttribute("currentWiki",  selectedWiki);
        model.addAttribute("historyList",  historyList);
        model.addAttribute("relationList", relationList);
        model.addAttribute("searchResult", searchResult);
        model.addAttribute("keyword",      keyword);
        model.addAttribute("isVersionView", false);

        return "weple/wiki/detail";
    }

    //  특정 버전 조회
    @GetMapping("/project/wiki/version")
    public String wikiVersionPage(
            @RequestParam Long    projectId,
            @RequestParam String  wikiPageId,
            @RequestParam Integer version,
            Model model) {

        List<WikiPageVO>     wikiTree    = wikiService.findWikiTree(projectId);
        WikiHistoryVO        history     = wikiService.findHistoryByVersion(wikiPageId, version);
        WikiPageVO           currentWiki = wikiService.findWikiById(wikiPageId);
        List<WikiHistoryVO>  historyList = wikiService.findHistoryList(wikiPageId);
        List<WikiRelationVO> relationList = wikiService.findRelationList(wikiPageId);

        // 히스토리 → WikiPageVO 변환 (본문 표시용)
        WikiPageVO viewWiki = null;
        if (history != null && currentWiki != null) {
            viewWiki = new WikiPageVO();
            viewWiki.setWikiPageId(currentWiki.getWikiPageId());
            viewWiki.setProjectId(projectId);
            viewWiki.setTitle(history.getTitle());
            viewWiki.setContent(history.getContent());
            viewWiki.setCurrentVersion(history.getWikiVersion());
            viewWiki.setUserName(history.getUserName());
            viewWiki.setUserEmail(currentWiki.getUserEmail());
            viewWiki.setUpdateAt(history.getCreatedAt());
        }

        setCommonModel(model, projectId);
        model.addAttribute("wikiTree",     wikiTree);
        model.addAttribute("selectedWiki", viewWiki);
        model.addAttribute("currentWiki",  currentWiki);
        model.addAttribute("historyList",  historyList);
        model.addAttribute("relationList", relationList);
        model.addAttribute("searchResult", null);
        model.addAttribute("keyword",      null);
        model.addAttribute("isVersionView", true);

        return "weple/wiki/detail";
    }

    //  위키 등록 페이지
    @GetMapping("/project/wiki/register")
    public String wikiRegisterForm(
            @RequestParam Long   projectId,
            @RequestParam(required = false) String parentPageId,
            Model model) {

        setCommonModel(model, projectId);
        model.addAttribute("wikiTree",     wikiService.findWikiTree(projectId));
        model.addAttribute("parentPageId", parentPageId);

        return "weple/wiki/register";
    }

    //  위키 등록 처리
    //  relations JSON 파싱 후 wiki_relations 저장
    @PostMapping("/project/wiki/register")
    public String wikiRegisterProcess(
            @RequestParam Long   projectId,
            @RequestParam(required = false, defaultValue = "[]") String relations,
            WikiPageVO vo,
            @AuthenticationPrincipal LoginUserDetails loginUser) {

        vo.setProjectId(projectId);
        vo.setUserCode(loginUser.getLoginUser().getUserCode());
        wikiService.insertWikiWithRelations(vo, relations);

        return "redirect:/project/wiki?projectId=" + projectId;
    }

    //  위키 수정 페이지 
    //  편집 잠금 획득
    @GetMapping("/project/wiki/modify/{wikiPageId}")
    public String wikiModifyForm(
            @PathVariable String wikiPageId,
            @RequestParam Long   projectId,
            Model model,
            @AuthenticationPrincipal LoginUserDetails loginUser) {

        WikiPageVO wiki = wikiService.findWikiById(wikiPageId);

        // 편집 잠금 시도
        String myUserCode = loginUser.getLoginUser().getUserCode();
        boolean locked = wikiService.lockWiki(wikiPageId, myUserCode);

        if (!locked) {
            // 다른 사람이 이미 잠금 → 잠금 정보 조회 후 조회 페이지로 리다이렉트
            WikiPageVO lockInfo = wikiService.getLockInfo(wikiPageId);
            model.addAttribute("lockError",    true);
            model.addAttribute("lockUserName", lockInfo != null ? lockInfo.getLockUserName() : "다른 사용자");
            model.addAttribute("lockedAt",     lockInfo != null ? lockInfo.getLockedAt()     : null);
            return "redirect:/project/wiki?projectId=" + projectId
                   + "&wikiPageId=" + wikiPageId
                   + "&lockError=true";
        }

        // 잠금 성공 → 잠금 정보 포함 wiki 재조회 (lockUserName, lockedAt 포함)
        wiki = wikiService.findWikiById(wikiPageId);
        WikiPageVO lockInfo = wikiService.getLockInfo(wikiPageId);

        List<WikiHistoryVO>  historyList  = wikiService.findHistoryList(wikiPageId);
        List<WikiRelationVO> relationList = wikiService.findRelationList(wikiPageId);

        setCommonModel(model, projectId);
        model.addAttribute("wiki",         wiki);
        model.addAttribute("lockInfo",     lockInfo);      // 배너용: lockUserName, lockedAt
        model.addAttribute("wikiTree",     wikiService.findWikiTree(projectId));
        model.addAttribute("historyList",  historyList);
        model.addAttribute("relationList", relationList);

        return "weple/wiki/modify";
    }

    //  위키 수정 처리 
    //  저장 완료 시 잠금 해제 (서비스에서 처리)
    @PostMapping("/project/wiki/modify")
    public String wikiModifyProcess(
            @RequestParam Long projectId,
            WikiPageVO vo,
            @AuthenticationPrincipal LoginUserDetails loginUser) {

        vo.setProjectId(projectId);
        vo.setUserCode(loginUser.getLoginUser().getUserCode());
        wikiService.updateWiki(vo);  // 내부: 프로시저 호출 후 unlockWiki

        return "redirect:/project/wiki?projectId=" + projectId + "&wikiPageId=" + vo.getWikiPageId();
    }

    //  취소 시 잠금 해제
    @PostMapping("/project/wiki/unlock")
    @ResponseBody
    public ResponseEntity<Void> unlockWiki(
            @RequestParam String wikiPageId,
            @AuthenticationPrincipal LoginUserDetails loginUser) {

        wikiService.unlockWiki(wikiPageId, loginUser.getLoginUser().getUserCode());
        return ResponseEntity.ok().build();
    }

    //  위키 삭제  POST
    @PostMapping("/project/wiki/delete/{wikiPageId}")
    public String wikiDelete(
            @PathVariable String wikiPageId,
            @RequestParam Long   projectId,
            @AuthenticationPrincipal LoginUserDetails loginUser) {

        // 잠금 해제 후 삭제 (관계 포함)
        wikiService.unlockWiki(wikiPageId, loginUser.getLoginUser().getUserCode());
        wikiService.deleteWiki(wikiPageId);
        return "redirect:/project/wiki?projectId=" + projectId;
    }

    //  특정 버전 조회 (AJAX)
    @GetMapping("/project/wiki/history")
    @ResponseBody
    public ResponseEntity<WikiHistoryVO> getHistoryVersion(
            @RequestParam String  wikiPageId,
            @RequestParam Integer wikiVersion) {

        WikiHistoryVO history = wikiService.findHistoryByVersion(wikiPageId, wikiVersion);
        return history == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(history);
    }

    //  해시태그 자동완성
    @GetMapping("/project/wiki/hashtag/search")
    @ResponseBody
    public ResponseEntity<List<WikiRelationVO>> hashtagSearch(
            @RequestParam Long   projectId,
            @RequestParam String keyword,
            @RequestParam(required = false, defaultValue = "") String type) {

        List<WikiRelationVO> result = wikiService.searchByHashtag(projectId, keyword, type);
        return ResponseEntity.ok(result);
    }

    //  연관문서 추가
    @PostMapping("/project/wiki/relation/add")
    @ResponseBody
    public ResponseEntity<Void> addRelation(@RequestBody WikiRelationVO vo) {
        wikiService.addRelation(vo);
        return ResponseEntity.ok().build();
    }

    //  연관문서 삭제
    @DeleteMapping("/project/wiki/relation/{wikiRelationId}")
    @ResponseBody
    public ResponseEntity<Void> removeRelation(@PathVariable String wikiRelationId) {
        wikiService.removeRelation(wikiRelationId);
        return ResponseEntity.ok().build();
    }
}