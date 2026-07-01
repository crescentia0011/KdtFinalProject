package com.weple.cloud.mypage.web;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.weple.cloud.auth.service.LoginUserDetails;
import com.weple.cloud.mypage.service.MyPageNotificationUpdateVO;
import com.weple.cloud.mypage.service.MyPageService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class MyPageController {

    private final MyPageService myPageService;

    // 마이페이지 메인 (헤더 프로필 아이콘 클릭 시 진입)
    @GetMapping("/mypage")
    public String main(@AuthenticationPrincipal LoginUserDetails loginUser, Model model) {
        String userCode = loginUser.getLoginUser().getUserCode();
        model.addAttribute("myPage", myPageService.findMyPage(userCode));
        model.addAttribute("sidebarMenu", "none");
        model.addAttribute("currentMenu", "mypage");
        return "weple/mypage/main";
    }

    // 등록 버튼 - 알림 설정 저장
    @PostMapping("/mypage/notification")
    public String updateNotification(@AuthenticationPrincipal LoginUserDetails loginUser,
            @ModelAttribute MyPageNotificationUpdateVO request,
            RedirectAttributes redirectAttributes) {
        request.setUserCode(loginUser.getLoginUser().getUserCode());
        try {
            myPageService.updateNotificationSetting(request);

            // saveProfile()에서 profileImage를 세션에 즉시 반영하는 것과 동일하게,
            // 저장된 알림 설정도 세션(LoginUserVO)에 바로 반영해야 헤더의 실시간 알림(JS)이
            // 재로그인 없이 곧바로 새 설정을 따른다.
            loginUser.getLoginUser().setWebNotificationYn(request.getWebNotificationYn());
            loginUser.getLoginUser().setEmailNotificationYn(request.getEmailNotificationYn());
            loginUser.getLoginUser().setNotificationArea(request.getNotificationArea());

            redirectAttributes.addFlashAttribute("mypageSuccess", "설정이 저장되었습니다.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("mypageError", ex.getMessage());
        }
        return "redirect:/mypage";
    }

    // 프로필 사진 관리 화면 (마이페이지 카메라 아이콘 클릭 시 진입)
    @GetMapping("/mypage/profile")
    public String profile(@AuthenticationPrincipal LoginUserDetails loginUser, Model model) {
        String userCode = loginUser.getLoginUser().getUserCode();
        model.addAttribute("myPageProfile", myPageService.findMyPageProfile(userCode));
        model.addAttribute("sidebarMenu", "none");
        model.addAttribute("currentMenu", "mypage");
        return "weple/mypage/profile";
    }

    @PostMapping("/mypage/profile/save")
    public String saveProfile(@AuthenticationPrincipal LoginUserDetails loginUser,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "removeFlag", defaultValue = "N") String removeFlag,
            RedirectAttributes redirectAttributes) {
        String userCode = loginUser.getLoginUser().getUserCode();
        try {
            if (file != null && !file.isEmpty()) {
                String savedImageUrl = myPageService.updateProfileImage(userCode, file);
                loginUser.getLoginUser().setProfileImage(savedImageUrl);
            } else if ("Y".equals(removeFlag)) {
                myPageService.deleteProfileImage(userCode);
                loginUser.getLoginUser().setProfileImage(null);
            }
            redirectAttributes.addFlashAttribute("mypageSuccess", "프로필 사진이 저장되었습니다.");
            return "redirect:/mypage";
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("profileError", ex.getMessage());
            return "redirect:/mypage/profile";
        }
    }
}