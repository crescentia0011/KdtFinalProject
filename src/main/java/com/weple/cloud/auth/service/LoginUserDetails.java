package com.weple.cloud.auth.service;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class LoginUserDetails implements UserDetails {

    // 로그인한 사용자 기본 정보
    private final LoginUserVO loginUser;

    // Spring Security에서 사용할 권한 목록
    private final Collection<? extends GrantedAuthority> authorities;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 사용자에게 부여된 권한 목록 반환
        return authorities;
    }

    @Override
    public String getPassword() {
        // DB에 저장된 BCrypt 비밀번호 반환
        return loginUser.getPassword();
    }

    @Override
    public String getUsername() {
        // Spring Security에서 username으로 사용할 로그인 아이디 반환
        return loginUser.getLoginId();
    }

    @Override
    public boolean isEnabled() {
        // 상태별 로그인 차단은 LoginUserDetailsServiceImpl에서 처리
        return true;
    }
}
