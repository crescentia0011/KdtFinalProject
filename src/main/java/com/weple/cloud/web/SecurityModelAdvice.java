package com.weple.cloud.web;

import java.util.Collections;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.weple.cloud.auth.service.LoginUserDetails;
import com.weple.cloud.auth.service.LoginUserVO;
import com.weple.cloud.project.service.ProjectService;
import com.weple.cloud.repository.service.RepositoryService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@ControllerAdvice(annotations = Controller.class)
@RequiredArgsConstructor
public class SecurityModelAdvice {

    private final RepositoryService repositoryService;
    private final ProjectService projectService;

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
    
    // 공통 헤더에 프로필 아이콘/이름에서 사용할 로그인 사용자 정보-은지
    @ModelAttribute("currentUser")
    public LoginUserVO currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUserDetails)) {
            return null;
        }
        return ((LoginUserDetails) authentication.getPrincipal()).getLoginUser();
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
    
    // 선택한 모듈만 노출 - 은지
    @ModelAttribute("moduleNames")
    public List<String> moduleNames(HttpServletRequest request) {
        String projectId = request.getParameter("projectId");
 
        // Path Variable도 처리 (/project/{id}/setting 등)
        if (projectId == null) {
            String uri = request.getRequestURI();
            java.util.regex.Matcher m =
                java.util.regex.Pattern.compile("/project/(\\d+)").matcher(uri);
            if (m.find()) projectId = m.group(1);
        }
 
        if (projectId == null) return Collections.emptyList();
        try {
            return projectService.findActiveModuleNames(Long.valueOf(projectId));
        } catch (NumberFormatException e) {
            return Collections.emptyList();
        }
    }
}
