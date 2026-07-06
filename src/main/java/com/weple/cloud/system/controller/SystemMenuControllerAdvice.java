package com.weple.cloud.system.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.weple.cloud.auth.service.LoginUserDetails;
import com.weple.cloud.auth.service.LoginUserVO;
import com.weple.cloud.project.service.ProjectService;

import lombok.RequiredArgsConstructor;

@ControllerAdvice(annotations = org.springframework.stereotype.Controller.class)
@RequiredArgsConstructor
public class SystemMenuControllerAdvice {

    private final ProjectService projectService;

    private boolean isCompanyManager(LoginUserVO user) {
        return Integer.valueOf(1).equals(user.getOwnerYn())
            || Integer.valueOf(1).equals(user.getAdminYn());
    }

    @ModelAttribute("canManageSystem")
    public boolean canManageSystem(@AuthenticationPrincipal LoginUserDetails loginUser) {
        if (loginUser == null) {
            return false; // 비로그인(로그인 화면 등)
        }

        LoginUserVO user = loginUser.getLoginUser();

        if (isCompanyManager(user)) {
            return true;
        }

        // 프로젝트 관리 권한(k1_create)을 하나라도 가지고 있으면 "관리" 메뉴 진입 허용
        return !projectService.findAnyProjectPermissionCodes(user.getUserCode()).isEmpty();
    }
}