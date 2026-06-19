package com.weple.cloud.auth.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.weple.cloud.auth.mapper.SignupMapper;
import com.weple.cloud.auth.service.SignupRequestVO;
import com.weple.cloud.auth.service.SignupService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SignupServiceImpl implements SignupService {

    private static final int DEFAULT_MIN_PASSWORD_LENGTH = 8;

    private final SignupMapper signupMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void signup(SignupRequestVO request) {
        validate(request);
        normalize(request);

        // 회사 코드가 실제로 존재하는지 확인하고 USERS.COMPANY_ID에 저장할 값을 찾습니다.
        Long companyId = signupMapper.selectCompanyIdByCompanyCode(request.getCompanyCode());
        if (companyId == null) {
            throw new IllegalArgumentException("존재하지 않는 회사 코드입니다.");
        }

        // 로그인 아이디는 중복될 수 없으므로 저장 전에 한 번 더 확인합니다.
        if (signupMapper.countUserByLoginId(request.getLoginId()) > 0) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        // 비밀번호는 DB에 원문이 아니라 BCrypt 암호화 값으로 저장합니다.
        request.setCompanyId(companyId);
        request.setEncodedPassword(passwordEncoder.encode(request.getPassword()));

        int result = signupMapper.insertSignupUser(request);
        if (result != 1) {
            throw new IllegalStateException("회원가입 처리 중 오류가 발생했습니다.");
        }
    }

    private void validate(SignupRequestVO request) {
        requireText(request.getCompanyCode(), "회사 코드를 입력해주세요.");
        requireText(request.getUserName(), "이름을 입력해주세요.");
        requireText(request.getLoginId(), "아이디를 입력해주세요.");
        requireText(request.getPassword(), "비밀번호를 입력해주세요.");
        requireText(request.getPasswordConfirm(), "비밀번호 확인을 입력해주세요.");
        requireText(request.getEmail(), "이메일을 입력해주세요.");
        requireText(request.getPhoneNumber(), "연락처를 입력해주세요.");

        if (!request.getLoginId().matches("^[a-zA-Z0-9._-]{4,30}$")) {
            throw new IllegalArgumentException("아이디는 영문, 숫자, 특수문자 ._- 조합 4~30자로 입력해주세요.");
        }

        // 최소 비밀번호 길이는 관리 설정 기능이 완성되면 설정값으로 교체하면 됩니다.
        if (request.getPassword().length() < DEFAULT_MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("비밀번호는 최소 " + DEFAULT_MIN_PASSWORD_LENGTH + "자 이상 입력해주세요.");
        }

        if (!request.getPassword().equals(request.getPasswordConfirm())) {
            throw new IllegalArgumentException("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }

        if (!request.getEmail().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("이메일 형식이 올바르지 않습니다.");
        }

        if (!request.getPhoneNumber().matches("^\\d{2,3}-?\\d{3,4}-?\\d{4}$")) {
            throw new IllegalArgumentException("연락처 형식이 올바르지 않습니다.");
        }
    }

    private void requireText(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

    private void normalize(SignupRequestVO request) {
        request.setCompanyCode(request.getCompanyCode().trim());
        request.setUserName(request.getUserName().trim());
        request.setLoginId(request.getLoginId().trim());
        request.setEmail(request.getEmail().trim());
        request.setPhoneNumber(request.getPhoneNumber().trim());
    }

}
