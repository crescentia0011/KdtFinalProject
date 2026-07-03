package com.weple.cloud.auth.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import com.weple.cloud.auth.service.SignupRequestVO;
import com.weple.cloud.auth.service.SignupService;

class AuthWebControllerTest {

    private final SignupService signupService = Mockito.mock(SignupService.class);
    private final AuthWebController controller = new AuthWebController(signupService);

    @Test
    @DisplayName("회사별 회원가입 URL의 회사코드와 입력 회사코드가 다르면 다시 회원가입 화면으로 보낸다")
    void companyJoinRejectsMismatchedCompanyCode() {
        SignupRequestVO request = validRequest();
        request.setCompanyCode("CP-002");
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

        String viewName = controller.companyJoin("CP-001", request, redirectAttributes);

        assertThat(viewName).isEqualTo("redirect:/c/CP-001/join");
        assertThat(redirectAttributes.getFlashAttributes())
                .containsKeys("joinError", "signupRequest");
        verify(signupService, never()).signup(any());
    }

    @Test
    @DisplayName("회사별 회원가입 URL과 입력 회사코드가 같으면 가입 처리 후 회사별 로그인으로 보낸다")
    void companyJoinRedirectsToCompanyLoginAfterSuccess() {
        SignupRequestVO request = validRequest();
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

        String viewName = controller.companyJoin("CP-001", request, redirectAttributes);

        assertThat(viewName).isEqualTo("redirect:/c/CP-001/login");
        assertThat(redirectAttributes.getFlashAttributes()).containsKey("joinSuccess");
        verify(signupService).signup(request);
    }

    @Test
    @DisplayName("기본 회원가입 성공 시 기본 로그인 화면으로 보낸다")
    void joinRedirectsToLoginAfterSuccess() {
        SignupRequestVO request = validRequest();
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

        String viewName = controller.join(request, redirectAttributes);

        assertThat(viewName).isEqualTo("redirect:/login");
        assertThat(redirectAttributes.getFlashAttributes()).containsKey("joinSuccess");
        verify(signupService).signup(request);
    }

    private SignupRequestVO validRequest() {
        SignupRequestVO request = new SignupRequestVO();
        request.setCompanyCode("CP-001");
        request.setUserName("테스트사용자");
        request.setLoginId("signup_test01");
        request.setPassword("test1234!");
        request.setPasswordConfirm("test1234!");
        request.setEmail("signup_test01@weple.com");
        request.setPhoneNumber("010-1234-5678");
        return request;
    }
}
