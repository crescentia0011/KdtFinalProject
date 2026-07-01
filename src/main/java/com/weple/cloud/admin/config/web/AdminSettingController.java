package com.weple.cloud.admin.config.web;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.weple.cloud.auth.service.LoginUserDetails;
import com.weple.cloud.repository.service.RepositoryManageSettingVO;
import com.weple.cloud.repository.service.RepositoryService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class AdminSettingController {

    private final RepositoryService repositoryService;

    // 관리 메뉴 설정 탭에서 회사별 저장소 설정 화면 보여줌
    @GetMapping("/settingDetail")
    public String repositorySettingDetail(@AuthenticationPrincipal LoginUserDetails loginUser, Model model) {
        // 저장 실패 후 되돌아온 입력값이 있으면 그 값을 우선 표시함
        if (!model.containsAttribute("repositorySetting")) {
            model.addAttribute("repositorySetting",
                    repositoryService.findRepositoryManageSetting(loginUser.getLoginUser().getCompanyId()));
        }

        // 관리 헤더와 사이드바 활성 표시 필요함
        model.addAttribute("sidebarMenu", "system");
        model.addAttribute("currentMenu", "setting");
        model.addAttribute("menu", "setting");
        model.addAttribute("settingTab", "repository");
        return "weple/admin/config/repo-setting";
    }

    // 관리 설정 화면에서 입력한 저장소 커밋 연결 규칙 저장함
    @PostMapping("/settingDetail")
    public String saveRepositorySetting(@AuthenticationPrincipal LoginUserDetails loginUser,
                                        @ModelAttribute RepositoryManageSettingVO repositorySetting,
                                        RedirectAttributes redirectAttributes) {
        // 화면에 노출하지 않은 회사 코드는 로그인 사용자 기준으로 고정함
        repositorySetting.setCompanyId(loginUser.getLoginUser().getCompanyId());
        try {
            repositoryService.saveRepositoryManageSetting(repositorySetting);
            redirectAttributes.addFlashAttribute("settingSuccess", "저장소 설정이 저장되었습니다.");
        } catch (IllegalArgumentException ex) {
            // 검증 실패 시 사용자가 입력한 값 유지 필요함
            redirectAttributes.addFlashAttribute("settingError", ex.getMessage());
            redirectAttributes.addFlashAttribute("repositorySetting", repositorySetting);
        }
        return "redirect:/settingDetail";
    }
}
