package com.weple.cloud.auth.impl;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class LoginFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {

        // 로그인 실패 시 화면에 표시할 기본 메시지
        String message = "아이디 또는 비밀번호가 일치하지 않습니다.";

        // 비밀번호 불일치가 아닌 경우 서비스에서 던진 메시지를 사용
        if (!(exception instanceof BadCredentialsException) && exception.getMessage() != null) {
            message = exception.getMessage();
        }

        // 한글 메시지를 URL 파라미터로 전달하기 위해 인코딩
        String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8);

        // 로그인 화면으로 실패 메시지 전달
        response.sendRedirect(request.getContextPath() + "/login?error=" + encodedMessage);
    }
}
