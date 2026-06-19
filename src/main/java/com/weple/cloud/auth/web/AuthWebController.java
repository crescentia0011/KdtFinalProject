package com.weple.cloud.auth.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.weple.cloud.auth.service.SignupRequestVO;
import com.weple.cloud.auth.service.SignupService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class AuthWebController {

    private final SignupService signupService;

    // 로그인 화면 이동
    @GetMapping("/login")
    public String loginPage() {
        return "weple/auth/login";
    }

    // 회원가입 화면 이동
    @GetMapping("/join")
    public String joinPage() {
        return "weple/auth/join";
    }

    // 회원가입 폼에서 입력한 값을 검증하고 가입 승인 대기 계정을 생성
    @PostMapping("/join")
    public String join(SignupRequestVO request, RedirectAttributes redirectAttributes) {
        try {
            signupService.signup(request);
            redirectAttributes.addFlashAttribute("joinSuccess", "회원가입 요청이 완료되었습니다. 관리자 승인 후 로그인할 수 있습니다.");
            return "redirect:/login";
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("joinError", ex.getMessage());
            redirectAttributes.addFlashAttribute("signupRequest", request);
            return "redirect:/join";
        }
    }
}
