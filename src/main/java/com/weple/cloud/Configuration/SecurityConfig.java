package com.weple.cloud.Configuration;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import com.weple.cloud.auth.impl.CompanyLoginValidationFilter;
import com.weple.cloud.auth.impl.LoginFailureHandler;
import com.weple.cloud.auth.impl.LoginSuccessHandler;
import com.weple.cloud.auth.impl.LoginUserDetailsServiceImpl;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt 방식으로 비밀번호 암호화/검증
        return new BCryptPasswordEncoder();
    }

    // 자동 로그인 토큰은 DB에 저장해 로그아웃·만료 시 서버에서도 무효화할 수 있게 합니다.
    @Bean
    public PersistentTokenRepository persistentTokenRepository(DataSource dataSource) {
        JdbcTokenRepositoryImpl tokenRepository = new JdbcTokenRepositoryImpl();
        tokenRepository.setDataSource(dataSource);
        return tokenRepository;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   LoginFailureHandler loginFailureHandler,
                                                   LoginSuccessHandler loginSuccessHandler,
                                                   CompanyLoginValidationFilter companyLoginValidationFilter,
                                                   PersistentTokenRepository persistentTokenRepository,
                                                   LoginUserDetailsServiceImpl loginUserDetailsService) throws Exception {

        http
            .authorizeHttpRequests(auth -> auth
                // 로그인 화면과 정적 리소스는 인증 없이 접근 허용
                .requestMatchers(
                    "/login",
                    "/join",
                    "/c/*/login",
                    "/c/*/join",
                    "/access-denide",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/assets/**"
                ).permitAll()

                // 가입승인 화면과 승인 처리는 기업 최고관리자 또는 관리자만 접근할 수 있음
                .requestMatchers("/approvalList", "/approvalList/**")
                .hasAnyAuthority("ROLE_COMPANY_OWNER", "ROLE_COMPANY_ADMIN")
                
                // 일감유형 화면과 일감유형 CRUD는 최고관리자 또는 관리자만 접근할 수 있다
                .requestMatchers("/system/taskType", "/system/taskType/**")
                .hasAnyAuthority("ROLE_COMPANY_OWNER", "ROLE_COMPANY_ADMIN")
                
                // 마일스톤 CUD는 k1_version 권한을 가진 사람만 접근할 수 있다
                .requestMatchers("/version/insert", "/insert", "/update", "/update-parent", "/delete")
                .authenticated()

                // 사용자 관리 목록과 활성·비활성 변경은 기업 최고관리자 또는 관리자만 처리할 수 있음
                .requestMatchers("/userList", "/userList/**")
                .hasAnyAuthority("ROLE_COMPANY_OWNER", "ROLE_COMPANY_ADMIN")

                // 저장소 화면은 프로젝트별 k4 권한을 컨트롤러에서 다시 확인함
                .requestMatchers("/repository/management", "/repository/management/**", "/repository", "/repository/update", "/repository/delete")
                .authenticated()
                // 관리 설정탭 접근은 기업 최고관리자 또는 관리자만 사용할 수 있음
                .requestMatchers("/settingDetail")
                .hasAnyAuthority("ROLE_COMPANY_OWNER", "ROLE_COMPANY_ADMIN")
                
                // 역할·권한 관리는 기업 최고관리자 또는 관리자만 접근할 수 있음-은
                .requestMatchers("/system/role", "/system/role/**")
                .hasAnyAuthority("ROLE_COMPANY_OWNER", "ROLE_COMPANY_ADMIN")
                
                // 관리 페이지 내부 프로젝트 생성·수정·목록은 기업 최고관리자 또는 관리자만 접근할 수 있음-은
                .requestMatchers("/system/project", "/system/project/**")
                .authenticated()

                // 관리-설정(모듈/일감유형 기본값 설정)은 기업 최고관리자만 접근할 수 있음
                .requestMatchers("/system/systemModules", "/system/systemModules/**")
                .hasAnyAuthority("ROLE_COMPANY_OWNER", "ROLE_COMPANY_ADMIN")
                
                // 관리 페이지 내부 그룹 관리는 기업 최고관리자 또는 관리자만 접근할 수 있음
                .requestMatchers("/groupList", "/groupInsert", "/groupDelete",
                                  "/groupUserList", "/groupUserInsert", "/groupUserUpdate", "/groupUserDelete")
                .hasAnyAuthority("ROLE_COMPANY_OWNER", "ROLE_COMPANY_ADMIN")
                
                // 관리 페이지 내부 코드값 관리는 기업 최고관리자 또는 관리자만 접근할 수 있음
                .requestMatchers("codeValueList", "/codeInsert", "codeUpdate", "/updateOrder")
                .hasAnyAuthority("ROLE_COMPANY_OWNER", "ROLE_COMPANY_ADMIN")
                
                // 그룹 관리·코드값 관리는 기업 최고관리자 또는 관리자만 접근할 수 있음
                .requestMatchers(
                    "/groupList", "/groupInsert", "/groupUpdate", "/groupDelete",
                    "/groupUserList", "/groupUserInsert", "/groupUserUpdate", "/groupUserDelete",
                    "/codeValueList", "/codeInsert", "/codeUpdate", "/codeDelete", "/updateOrder"
                )
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

                // 로그인 성공 시 최근 로그인 일시를 저장한 뒤 메인으로 이동
                .successHandler(loginSuccessHandler)

                // 로그인 실패 시 메시지를 구분해서 처리
                .failureHandler(loginFailureHandler)

                // 로그인 관련 URL 접근 허용
                .permitAll()
            )
            // 자동 로그인 체크 시에만 DB 검증용 토큰 쿠키를 발급합니다.
            .rememberMe(remember -> remember
                .rememberMeParameter("rememberMe")
                .tokenValiditySeconds(60 * 60 * 24 * 14)
                .tokenRepository(persistentTokenRepository)
                .userDetailsService(loginUserDetailsService)
                .rememberMeCookieName("WEPLE_REMEMBER_ME")
                .alwaysRemember(false)
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
            )
            .addFilterBefore(companyLoginValidationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}