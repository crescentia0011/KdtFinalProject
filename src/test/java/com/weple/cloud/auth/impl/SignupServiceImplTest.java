package com.weple.cloud.auth.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.weple.cloud.auth.mapper.SignupMapper;
import com.weple.cloud.auth.service.SignupRequestVO;

@ExtendWith(MockitoExtension.class)
class SignupServiceImplTest {

    @Mock
    private SignupMapper signupMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    private SignupServiceImpl signupService;

    @BeforeEach
    void setUp() {
        signupService = new SignupServiceImpl(signupMapper, passwordEncoder);
    }

    @Test
    @DisplayName("필수값이 비어 있으면 회원가입 요청을 저장하지 않는다")
    void signupRejectsBlankRequiredValue() {
        SignupRequestVO request = validRequest();
        request.setLoginId(" ");

        assertThatThrownBy(() -> signupService.signup(request))
                .isInstanceOf(IllegalArgumentException.class);

        verify(signupMapper, never()).insertSignupUser(any());
    }

    @Test
    @DisplayName("존재하지 않는 회사코드이면 회원가입 요청을 저장하지 않는다")
    void signupRejectsUnknownCompanyCode() {
        SignupRequestVO request = validRequest();
        when(signupMapper.selectCompanyIdByCompanyCode("CP-001")).thenReturn(null);

        assertThatThrownBy(() -> signupService.signup(request))
                .isInstanceOf(IllegalArgumentException.class);

        verify(signupMapper, never()).insertSignupUser(any());
    }

    @Test
    @DisplayName("이미 사용 중인 아이디이면 회원가입 요청을 저장하지 않는다")
    void signupRejectsDuplicatedLoginId() {
        SignupRequestVO request = validRequest();
        when(signupMapper.selectCompanyIdByCompanyCode("CP-001")).thenReturn(1L);
        when(signupMapper.countUserByLoginId("signup_test01")).thenReturn(1);

        assertThatThrownBy(() -> signupService.signup(request))
                .isInstanceOf(IllegalArgumentException.class);

        verify(signupMapper, never()).insertSignupUser(any());
    }

    @Test
    @DisplayName("이미 사용 중인 이메일이면 회원가입 요청을 저장하지 않는다")
    void signupRejectsDuplicatedEmail() {
        SignupRequestVO request = validRequest();
        when(signupMapper.selectCompanyIdByCompanyCode("CP-001")).thenReturn(1L);
        when(signupMapper.countUserByLoginId("signup_test01")).thenReturn(0);
        when(signupMapper.countUserByEmail("signup_test01@weple.com")).thenReturn(1);

        assertThatThrownBy(() -> signupService.signup(request))
                .isInstanceOf(IllegalArgumentException.class);

        verify(signupMapper, never()).insertSignupUser(any());
    }

    @Test
    @DisplayName("비밀번호가 최소 길이보다 짧으면 회원가입 요청을 저장하지 않는다")
    void signupRejectsShortPassword() {
        SignupRequestVO request = validRequest();
        request.setPassword("1234");
        request.setPasswordConfirm("1234");

        assertThatThrownBy(() -> signupService.signup(request))
                .isInstanceOf(IllegalArgumentException.class);

        verify(signupMapper, never()).insertSignupUser(any());
    }

    @Test
    @DisplayName("비밀번호와 비밀번호 확인이 다르면 회원가입 요청을 저장하지 않는다")
    void signupRejectsPasswordMismatch() {
        SignupRequestVO request = validRequest();
        request.setPasswordConfirm("test12345!");

        assertThatThrownBy(() -> signupService.signup(request))
                .isInstanceOf(IllegalArgumentException.class);

        verify(signupMapper, never()).insertSignupUser(any());
    }

    @Test
    @DisplayName("이메일 형식이 올바르지 않으면 회원가입 요청을 저장하지 않는다")
    void signupRejectsInvalidEmailFormat() {
        SignupRequestVO request = validRequest();
        request.setEmail("wrong-mail");

        assertThatThrownBy(() -> signupService.signup(request))
                .isInstanceOf(IllegalArgumentException.class);

        verify(signupMapper, never()).insertSignupUser(any());
    }

    @Test
    @DisplayName("연락처 형식이 올바르지 않으면 회원가입 요청을 저장하지 않는다")
    void signupRejectsInvalidPhoneNumberFormat() {
        SignupRequestVO request = validRequest();
        request.setPhoneNumber("1111111");

        assertThatThrownBy(() -> signupService.signup(request))
                .isInstanceOf(IllegalArgumentException.class);

        verify(signupMapper, never()).insertSignupUser(any());
    }

    @Test
    @DisplayName("정상 입력이면 비밀번호를 암호화하고 승인대기 계정 저장을 요청한다")
    void signupEncodesPasswordAndInsertsPendingUser() {
        SignupRequestVO request = validRequest();
        when(signupMapper.selectCompanyIdByCompanyCode("CP-001")).thenReturn(1L);
        when(signupMapper.countUserByLoginId("signup_test01")).thenReturn(0);
        when(signupMapper.countUserByEmail("signup_test01@weple.com")).thenReturn(0);
        when(passwordEncoder.encode("test1234!")).thenReturn("$2a$encoded-password");
        when(signupMapper.insertSignupUser(any(SignupRequestVO.class))).thenReturn(1);

        signupService.signup(request);

        ArgumentCaptor<SignupRequestVO> captor = ArgumentCaptor.forClass(SignupRequestVO.class);
        verify(signupMapper).insertSignupUser(captor.capture());
        SignupRequestVO savedRequest = captor.getValue();

        assertThat(savedRequest.getCompanyId()).isEqualTo(1L);
        assertThat(savedRequest.getCompanyCode()).isEqualTo("CP-001");
        assertThat(savedRequest.getLoginId()).isEqualTo("signup_test01");
        assertThat(savedRequest.getEmail()).isEqualTo("signup_test01@weple.com");
        assertThat(savedRequest.getEncodedPassword()).isEqualTo("$2a$encoded-password");
        assertThat(savedRequest.getEncodedPassword()).isNotEqualTo(savedRequest.getPassword());
    }

    @Test
    @DisplayName("저장 결과가 1건이 아니면 회원가입 처리 오류로 판단한다")
    void signupRejectsUnexpectedInsertResult() {
        SignupRequestVO request = validRequest();
        when(signupMapper.selectCompanyIdByCompanyCode("CP-001")).thenReturn(1L);
        when(signupMapper.countUserByLoginId("signup_test01")).thenReturn(0);
        when(signupMapper.countUserByEmail("signup_test01@weple.com")).thenReturn(0);
        when(passwordEncoder.encode("test1234!")).thenReturn("$2a$encoded-password");
        when(signupMapper.insertSignupUser(any(SignupRequestVO.class))).thenReturn(0);

        assertThatThrownBy(() -> signupService.signup(request))
                .isInstanceOf(IllegalStateException.class);
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
