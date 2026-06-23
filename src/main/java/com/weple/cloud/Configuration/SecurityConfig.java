package com.weple.cloud.Configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.weple.cloud.auth.impl.LoginFailureHandler;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt 방식으로 비밀번호 암호화/검증
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, LoginFailureHandler loginFailureHandler) throws Exception {

        http
            .authorizeHttpRequests(auth -> auth
                // 로그인 화면과 정적 리소스는 인증 없이 접근 허용
                .requestMatchers(
                    "/login",
                    "/join",
                    "/access-denide",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/assets/**"
                ).permitAll()

                // 가입승인 화면과 승인 처리는 기업 최고관리자 또는 관리자만 접근할 수 있습니다.
                .requestMatchers("/approvalList", "/approvalList/**")
                .hasAnyAuthority("ROLE_COMPANY_OWNER", "ROLE_COMPANY_ADMIN")
                
                // 일감유형 화면과 일감유형 CRUD는 최고관리자 또는 관리자만 접근할 수 있다
                .requestMatchers("/system/taskType", "/system/taskType/**")
                .hasAnyAuthority("ROLE_COMPANY_OWNER", "ROLE_COMPANY_ADMIN")

                // 저장소 등록 설정은 기업 최고관리자 또는 관리자만 사용할 수 있습니다.
                .requestMatchers("/repository/management", "/repository/management/**", "/repository", "/repository/update", "/repository/delete")
                .hasAnyAuthority("ROLE_COMPANY_OWNER", "ROLE_COMPANY_ADMIN")

                // 그 외 요청은 로그인 필요
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                // 우리가 만든 로그인 페이지
                .loginPage("/login")

                // 로그인 form이 POST로 요청할 URL
                .loginProcessingUrl("/login")

                // 로그인 폼의 아이디 input name
                .usernameParameter("loginId")

                // 로그인 폼의 비밀번호 input name
                .passwordParameter("password")

                // 로그인 성공 시 이동할 URL
                .defaultSuccessUrl("/", true)

                // 로그인 실패 시 메시지를 구분해서 처리
                .failureHandler(loginFailureHandler)

                // 로그인 관련 URL 접근 허용
                .permitAll()
            )
            .logout(logout -> logout
                // 로그아웃 처리 URL
                .logoutUrl("/logout")

                // 로그아웃 성공 메시지를 표시하기 위해 logout 파라미터를 붙여 이동
                .logoutSuccessUrl("/login?logout")

                // 로그아웃 관련 URL 접근 허용
                .permitAll()
            )
            .exceptionHandling(exception -> exception
                // 권한이 없는 요청은 브라우저 기본 403 화면 대신 안내 페이지로 이동합니다.
                .accessDeniedHandler((request, response, accessDeniedException) ->
                    response.sendRedirect(request.getContextPath() + "/access-denide")
                )
            );

        return http.build();
    }
}
