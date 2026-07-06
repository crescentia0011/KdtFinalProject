package com.weple.cloud.auth.impl;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.weple.cloud.auth.mapper.LoginMapper;
import com.weple.cloud.auth.service.LoginUserDetails;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final String COMPANY_LOGIN_ERROR_MESSAGE =
            "\uD68C\uC0AC \uB85C\uADF8\uC778 \uC8FC\uC18C\uC640 \uC0AC\uC6A9\uC790 \uD68C\uC0AC \uC815\uBCF4\uAC00 \uC77C\uCE58\uD558\uC9C0 \uC54A\uC2B5\uB2C8\uB2E4.";

    private final LoginMapper loginMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        Object principal = authentication.getPrincipal();

        if (principal instanceof LoginUserDetails loginUserDetails) {
            String requestedCompanyCode = resolveRequestedCompanyCode(request);
            String userCompanyCode = loginUserDetails.getLoginUser().getCompanyCode();

            // Re-check company scope after authentication as a defensive guard.
            if (requestedCompanyCode != null && !requestedCompanyCode.isBlank()
                    && !requestedCompanyCode.equalsIgnoreCase(userCompanyCode)) {
                SecurityContextHolder.clearContext();
                request.getSession().invalidate();

                String message = URLEncoder.encode(COMPANY_LOGIN_ERROR_MESSAGE, StandardCharsets.UTF_8);
                response.sendRedirect(request.getContextPath() + "/c/" + requestedCompanyCode + "/login?error=" + message);
                return;
            }

            // Save successful login time for the user detail page.
            loginMapper.updateLastLoginTime(loginUserDetails.getLoginUser().getUserCode());
            request.getSession().removeAttribute("LOGIN_COMPANY_CODE");
        }

        setDefaultTargetUrl("/dashboard");
        setAlwaysUseDefaultTargetUrl(true);
        super.onAuthenticationSuccess(request, response, authentication);
    }

    private String resolveRequestedCompanyCode(HttpServletRequest request) {
        String companyCode = request.getParameter("companyCode");
        if (companyCode != null && !companyCode.isBlank()) {
            return companyCode;
        }

        Object sessionCompanyCode = request.getSession().getAttribute("LOGIN_COMPANY_CODE");
        return sessionCompanyCode instanceof String value ? value : null;
    }
}
