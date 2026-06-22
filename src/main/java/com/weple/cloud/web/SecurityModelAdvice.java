package com.weple.cloud.web;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.weple.cloud.repository.service.RepositoryService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@ControllerAdvice(annotations = Controller.class)
@RequiredArgsConstructor
public class SecurityModelAdvice {

    private final RepositoryService repositoryService;

    // 상단 메뉴에서 가입승인 탭을 노출할 수 있는지 판단합니다.
    @ModelAttribute("canApproveSignup")
    public boolean canApproveSignup() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_COMPANY_OWNER".equals(authority.getAuthority())
                        || "ROLE_COMPANY_ADMIN".equals(authority.getAuthority()));
    }

    // 프로젝트에 저장소가 하나 이상 등록된 경우에만 저장소 탭을 노출합니다.
    @ModelAttribute("hasRepository")
    public boolean hasRepository(HttpServletRequest request) {
        String projectId = request.getParameter("projectId");
        Long currentProjectId = 1L;
        if (projectId != null) {
            try {
                currentProjectId = Long.valueOf(projectId);
            } catch (NumberFormatException ignored) {
                // 프로젝트 번호가 잘못된 요청은 현재 기본 프로젝트 기준으로 처리합니다.
            }
        }
        return repositoryService.hasRepository(currentProjectId);
    }
}
