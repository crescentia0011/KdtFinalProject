package com.weple.cloud.auth.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.weple.cloud.auth.service.LoginUserDetails;
import com.weple.cloud.auth.service.LoginUserResponse;
import com.weple.cloud.auth.service.LoginUserVO;

@RestController
public class AuthController {

    // 로그인 사용자 정보 조회 API
    @GetMapping("/api/auth/me")
    public LoginUserResponse me(@AuthenticationPrincipal LoginUserDetails loginUser) {
        // 인증 객체에서 로그인 사용자 정보 추출
        LoginUserVO user = loginUser.getLoginUser();

        // 비밀번호를 제외한 사용자 정보만 응답
        return new LoginUserResponse(
                user.getUserCode(),
                user.getCompanyId(),
                user.getLoginId(),
                user.getUserName(),
                user.getEmail(),
                user.getStatus(),
                user.getOwnerYn(),
                user.getAdminYn()
        );
    }
}
