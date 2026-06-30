package com.weple.cloud.auth.impl;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.weple.cloud.auth.mapper.LoginMapper;
import com.weple.cloud.auth.service.LoginUserVO;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CompanyLoginValidationFilter extends OncePerRequestFilter {

    private static final String LOGIN_COMPANY_CODE_SESSION_KEY = "LOGIN_COMPANY_CODE";
    private static final String COMPANY_CODE_REQUIRED_MESSAGE =
            "회사 코드를 입력해주세요.";
    private static final String COMPANY_LOGIN_ERROR_MESSAGE =
            "입력한 회사 코드와 사용자 회사 정보가 일치하지 않습니다.";
    private static final String COMPANY_URL_TAMPERED_MESSAGE =
            "접속한 회사 주소와 입력한 회사 코드가 일치하지 않습니다.";

    private final LoginMapper loginMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!isLoginRequest(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String sessionCompanyCode = resolveSessionCompanyCode(request);
        String requestedCompanyCode = resolveRequestedCompanyCode(request);
        if (requestedCompanyCode == null || requestedCompanyCode.isBlank()) {
            redirectLoginError(request, response, sessionCompanyCode, null, COMPANY_CODE_REQUIRED_MESSAGE);
            return;
        }

        /* 회사별 URL로 들어온 경우 화면값 조작 여부도 같이 확인함 */
        if (sessionCompanyCode != null && !sessionCompanyCode.equalsIgnoreCase(requestedCompanyCode)) {
            redirectLoginError(request, response, sessionCompanyCode, null, COMPANY_URL_TAMPERED_MESSAGE);
            return;
        }
        String loginCompanyCode = sessionCompanyCode == null ? requestedCompanyCode : sessionCompanyCode;

        String loginId = request.getParameter("loginId");
        if (loginId == null || loginId.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        LoginUserVO loginUser = loginMapper.selectLoginUserByLoginId(loginId);

        /* 비밀번호 인증 전에 회사코드와 사용자 회사가 같은지 먼저 확인함 */
        if (loginUser != null && !loginCompanyCode.equalsIgnoreCase(loginUser.getCompanyCode())) {
            redirectLoginError(request, response, sessionCompanyCode, requestedCompanyCode, COMPANY_LOGIN_ERROR_MESSAGE);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isLoginRequest(HttpServletRequest request) {
        return "POST".equalsIgnoreCase(request.getMethod())
                && (request.getContextPath() + "/login").equals(request.getRequestURI());
    }

    private String resolveRequestedCompanyCode(HttpServletRequest request) {
        String companyCode = request.getParameter("companyCode");
        return companyCode == null ? null : companyCode.trim();
    }

    private String resolveSessionCompanyCode(HttpServletRequest request) {
        Object companyCode = request.getSession().getAttribute(LOGIN_COMPANY_CODE_SESSION_KEY);
        return companyCode instanceof String value && !value.isBlank() ? value.trim() : null;
    }

    private void redirectLoginError(HttpServletRequest request,
                                    HttpServletResponse response,
                                    String lockedCompanyCode,
                                    String enteredCompanyCode,
                                    String errorMessage) throws IOException {
        String message = URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);
        String loginPath = lockedCompanyCode == null || lockedCompanyCode.isBlank()
                ? "/login"
                : "/c/" + encodePathSegment(lockedCompanyCode) + "/login";
        String companyCodeQuery = enteredCompanyCode == null || enteredCompanyCode.isBlank()
                ? ""
                : "&companyCode=" + encodeQueryValue(enteredCompanyCode);
        response.sendRedirect(request.getContextPath() + loginPath + "?error=" + message + companyCodeQuery);
    }

    private String encodePathSegment(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private String encodeQueryValue(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
