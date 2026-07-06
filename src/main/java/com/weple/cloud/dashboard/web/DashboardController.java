package com.weple.cloud.dashboard.web;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.weple.cloud.auth.service.LoginUserDetails;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    @GetMapping("/dashboard")
    public String dashboardMain(Model model) {
        // 사이드바 활성화용 변수만 바인딩 후 즉시 뷰 리턴
        model.addAttribute("sidebarMenu", "dashboard");
        return "weple/dashboard/main"; 
    }
}
