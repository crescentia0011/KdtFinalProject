package com.weple.cloud.repository.web;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.weple.cloud.auth.service.LoginUserDetails;
import com.weple.cloud.repository.service.RepositoryService;
import com.weple.cloud.repository.service.RepositoryVO;
import com.weple.cloud.repository.service.GithubRepositoryReader;
import com.weple.cloud.repository.service.GithubRepositoryInfo;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/repository")
public class RepositoryController {

    private final RepositoryService repositoryService;
    private final GithubRepositoryReader githubRepositoryReader;

    // GitHub 연동 설정 화면을 표시합니다.
    @GetMapping("/management")
    public String management(@RequestParam(defaultValue = "1") Long projectId, Model model) {
        setProjectMenu(model, projectId);
        // 검증 실패 후 돌아온 입력값이 있으면 유지하고, 처음 진입한 경우에만 새 객체를 만듭니다.
        RepositoryVO repository = model.containsAttribute("repository")
                ? (RepositoryVO) model.asMap().get("repository")
                : new RepositoryVO();
        repository.setProjectId(projectId);
        model.addAttribute("repository", repository);
        return "weple/repository/management";
    }

    // 관리자가 입력한 GitHub 저장소 정보를 현재 회사와 프로젝트에 등록합니다.
    @PostMapping
    public String registerRepository(@AuthenticationPrincipal LoginUserDetails loginUser,
                                     RepositoryVO repository,
                                     RedirectAttributes redirectAttributes) {
        try {
            // 회사 ID는 폼 값이 아닌 로그인한 관리자 정보에서만 사용합니다.
            repository.setCompanyId(loginUser.getLoginUser().getCompanyId());
            repositoryService.registerRepository(repository);
            redirectAttributes.addFlashAttribute("repositorySuccess", "저장소가 등록되었습니다.");
            return "redirect:/repository/list?projectId=" + repository.getProjectId();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("repositoryError", ex.getMessage());
            redirectAttributes.addFlashAttribute("repository", repository);
            return "redirect:/repository/management?projectId=" + repository.getProjectId();
        } catch (DataIntegrityViolationException ex) {
            // DB의 고유값·FK 제약 오류는 내부 SQL 대신 사용자가 이해할 수 있는 문구로 안내합니다.
            redirectAttributes.addFlashAttribute("repositoryError", "저장소 정보가 중복됐거나 연결 설정이 올바르지 않습니다.");
            redirectAttributes.addFlashAttribute("repository", repository);
            return "redirect:/repository/management?projectId=" + repository.getProjectId();
        }
    }

    // 프로젝트에 등록된 저장소를 현재 사용자의 회사 기준으로 조회합니다.
    @GetMapping("/list")
    public String list(@AuthenticationPrincipal LoginUserDetails loginUser,
                       @RequestParam(defaultValue = "1") Long projectId,
                       Model model) {
        setProjectMenu(model, projectId);
        model.addAttribute("repositoryList",
                repositoryService.findRepositories(loginUser.getLoginUser().getCompanyId(), projectId));
        return "weple/repository/list";
    }

    // 선택한 저장소의 기본 정보를 보여주며 GitHub 파일 조회는 다음 단계에서 연결합니다.
    @GetMapping("/detail")
    public String detail(@AuthenticationPrincipal LoginUserDetails loginUser,
                         @RequestParam(defaultValue = "1") Long projectId,
                         @RequestParam(required = false) String repositoryId,
                         @RequestParam(required = false) String branch,
                         @RequestParam(required = false) String directoryPath,
                         @RequestParam(required = false) String filePath,
                         Model model) {
        setProjectMenu(model, projectId);
        List<RepositoryVO> repositoryList =
                repositoryService.findRepositories(loginUser.getLoginUser().getCompanyId(), projectId);
        RepositoryVO repository = repositoryId == null && !repositoryList.isEmpty()
                ? repositoryList.get(0)
                : repositoryService.findRepository(loginUser.getLoginUser().getCompanyId(), projectId, repositoryId);
        model.addAttribute("repository", repository);
        if (repository != null) {
            // GitHub API가 실패해도 상세 화면의 기본 구조가 렌더링되도록 빈 정보를 먼저 준비합니다.
            GithubRepositoryInfo githubInfo = new GithubRepositoryInfo();
            githubInfo.setDefaultBranch("main");
            githubInfo.setSelectedBranch("main");
            githubInfo.setBranches(List.of("main"));
            githubInfo.setFiles(List.of());
            githubInfo.setSelectedFileContent("GitHub 파일 정보를 불러오는 중입니다.");
            githubInfo.setCommits(List.of());
            try {
                githubInfo = githubRepositoryReader.readRepository(
                        repository.getRepositoryUrl(), branch, directoryPath, filePath);
            } catch (IllegalStateException ex) {
                model.addAttribute("githubError", ex.getMessage());
            }
            model.addAttribute("githubInfo", githubInfo);
        }
        return "weple/repository/detail";
    }

    // GitHub 비교 API 연동 전에는 파일 변경 비교 화면만 먼저 제공합니다.
    @GetMapping("/diff")
    public String diff(@RequestParam(defaultValue = "1") Long projectId, Model model) {
        setProjectMenu(model, projectId);
        return "weple/repository/form";
    }

    // 프로젝트 공통 레이아웃에서 저장소 탭을 활성화합니다.
    private void setProjectMenu(Model model, Long projectId) {
        model.addAttribute("projectId", projectId);
        model.addAttribute("sidebarMenu", "project");
        model.addAttribute("currentMenu", "repository");
    }
}
