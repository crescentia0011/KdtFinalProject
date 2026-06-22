package com.weple.cloud.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AccessDeniedController {

    // 권한이 없는 사용자가 확인할 간단한 안내 화면입니다.
    @GetMapping("/access-denide")
    public String accessDeniedPage() {
        return "weple/access-denide";
    }
}
