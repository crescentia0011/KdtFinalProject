package com.weple.cloud.auth.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.weple.cloud.auth.service.SignupRequestVO;
import com.weple.cloud.auth.service.SignupService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class AuthWebController {

    private final SignupService signupService;

    // 로그인 화면 이동
    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String companyCode, Model model, HttpSession session) {
        session.removeAttribute("LOGIN_COMPANY_CODE");
        model.addAttribute("companyCode", companyCode);
        model.addAttribute("companyCodeLocked", false);
        model.addAttribute("companyCodeRequired", true);
        return "weple/auth/login";
    }

    // 회사별 로그인 주소로 진입하면 회사 코드를 로그인 폼에 함께 전달합니다.
    @GetMapping("/c/{companyCode}/login")
    public String companyLoginPage(@PathVariable String companyCode, Model model, HttpSession session) {
        session.setAttribute("LOGIN_COMPANY_CODE", companyCode);
        model.addAttribute("companyCode", companyCode);
        model.addAttribute("companyCodeLocked", true);
        model.addAttribute("companyCodeRequired", true);
        return "weple/auth/login";
    }

    // 회원가입 화면 이동
    @GetMapping("/join")
    public String joinPage() {
        return "weple/auth/join";
    }

    // 회사별 회원가입 주소로 진입하면 URL의 회사 코드를 화면에 기본값으로 표시합니다.
    @GetMapping("/c/{companyCode}/join")
    public String companyJoinPage(@PathVariable String companyCode, Model model) {
        SignupRequestVO request = new SignupRequestVO();
        request.setCompanyCode(companyCode);
        model.addAttribute("companyCode", companyCode);
        model.addAttribute("signupRequest", request);
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

    // 회사별 회원가입은 URL 회사코드와 입력 회사코드가 같은지 먼저 확인합니다.
    @PostMapping("/c/{companyCode}/join")
    public String companyJoin(@PathVariable String companyCode,
            SignupRequestVO request,
            RedirectAttributes redirectAttributes) {
        try {
            if (request.getCompanyCode() == null || !companyCode.equalsIgnoreCase(request.getCompanyCode().trim())) {
                throw new IllegalArgumentException("접속한 회사 주소와 입력한 회사 코드가 일치하지 않습니다.");
            }
            signupService.signup(request);
            redirectAttributes.addFlashAttribute("joinSuccess", "회원가입 요청이 완료되었습니다. 관리자 승인 후 로그인할 수 있습니다.");
            return "redirect:/c/" + companyCode + "/login";
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("joinError", ex.getMessage());
            redirectAttributes.addFlashAttribute("signupRequest", request);
            return "redirect:/c/" + companyCode + "/join";
        }
    }
}
