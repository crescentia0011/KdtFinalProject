package com.weple.cloud.system.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.weple.cloud.system.mapper.UserManagementMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserManagementServiceImpl implements UserManagementService {

    private static final String ACTIVE_STATUS = "a2";
    private static final String INACTIVE_STATUS = "a3";
    private static final int DEFAULT_MIN_PASSWORD_LENGTH = 8;

    private final UserManagementMapper userManagementMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<UserManagementVO> findUsers(Long companyId, String keyword, int offset, int pageSize) {
        return userManagementMapper.selectUserManagementList(companyId, keyword, offset, pageSize);
    }

    @Override
    public int countUsers(Long companyId, String keyword) {
        return userManagementMapper.countUserManagementList(companyId, keyword);
    }

    @Override
    @Transactional
    public void changeUserStatus(Long companyId, int actorOwnerYn, String userCode, String status) {
        validateStatusChange(companyId, userCode, status);

        // 최고관리자는 관리자를 포함해 변경할 수 있고, 일반 관리자는 일반 사용자만 변경할 수 있습니다.
        if (userManagementMapper.updateUserStatus(companyId, actorOwnerYn, userCode, status) != 1) {
            throw new IllegalArgumentException("기업최고관리자만 다른 관리자의 상태를 변경할 수 있습니다.");
        }
    }

    private void validateStatusChange(Long companyId, String userCode, String status) {
        if (companyId == null) {
            throw new IllegalArgumentException("회사 정보가 없습니다.");
        }
        if (userCode == null || userCode.isBlank()) {
            throw new IllegalArgumentException("사용자 정보가 없습니다.");
        }
        if (!ACTIVE_STATUS.equals(status) && !INACTIVE_STATUS.equals(status)) {
            throw new IllegalArgumentException("활성 또는 비활성 상태만 변경할 수 있습니다.");
        }
    }

    @Override
    @Transactional
    public void createUser(Long companyId, int actorOwnerYn, UserManagementCreateVO user) {
        validateCreateUser(companyId, actorOwnerYn, user);
        normalizeCreateUser(companyId, actorOwnerYn, user);

        // 로그인 아이디와 이메일은 USERS 전체에서 중복되지 않도록 저장 전에 확인합니다.
        if (userManagementMapper.countUserByLoginId(user.getLoginId()) > 0) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }
        if (userManagementMapper.countUserByEmail(user.getEmail()) > 0) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // DB에는 원문 비밀번호가 아니라 BCrypt 암호화 값을 저장합니다.
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        if (userManagementMapper.insertUser(user) != 1) {
            throw new IllegalStateException("신규 사용자 등록 중 오류가 발생했습니다.");
        }
    }

    private void validateCreateUser(Long companyId, int actorOwnerYn, UserManagementCreateVO user) {
        if (companyId == null) {
            throw new IllegalArgumentException("회사 정보가 없습니다.");
        }
        if (user == null) {
            throw new IllegalArgumentException("사용자 정보가 없습니다.");
        }

        requireText(user.getLoginId(), "사용자 아이디를 입력해주세요.");
        requireText(user.getPassword(), "비밀번호를 입력해주세요.");
        requireText(user.getPasswordConfirm(), "비밀번호 확인을 입력해주세요.");
        requireText(user.getUserName(), "이름을 입력해주세요.");
        requireText(user.getEmail(), "이메일을 입력해주세요.");
        requireText(user.getPhoneNumber(), "연락처를 입력해주세요.");

        if (!user.getLoginId().matches("^[a-zA-Z0-9._-]{4,30}$")) {
            throw new IllegalArgumentException("아이디는 영문, 숫자, 특수문자 ._- 조합 4~30자로 입력해주세요.");
        }
        if (user.getPassword().length() < DEFAULT_MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("비밀번호는 최소 " + DEFAULT_MIN_PASSWORD_LENGTH + "자 이상 입력해주세요.");
        }
        if (!user.getPassword().equals(user.getPasswordConfirm())) {
            throw new IllegalArgumentException("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }
        if (!user.getEmail().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("이메일 형식이 올바르지 않습니다.");
        }
        if (!user.getPhoneNumber().matches("^\\d{2,3}-?\\d{3,4}-?\\d{4}$")) {
            throw new IllegalArgumentException("연락처 형식이 올바르지 않습니다.");
        }
        if (actorOwnerYn != 1 && Integer.valueOf(1).equals(user.getAdminYn())) {
            throw new IllegalArgumentException("관리자 권한은 기업최고관리자만 부여할 수 있습니다.");
        }
    }

    private void normalizeCreateUser(Long companyId, int actorOwnerYn, UserManagementCreateVO user) {
        user.setCompanyId(companyId);
        user.setLoginId(user.getLoginId().trim());
        user.setUserName(user.getUserName().trim());
        user.setEmail(user.getEmail().trim());
        user.setPhoneNumber(user.getPhoneNumber().trim());

        // 일반 관리자가 임의로 관리자 계정을 만들지 못하도록 서버에서 한 번 더 보정합니다.
        user.setAdminYn(actorOwnerYn == 1 && Integer.valueOf(1).equals(user.getAdminYn()) ? 1 : 0);
        user.setWebNotificationYn(toYn(user.getWebNotificationYn()));
        user.setEmailNotificationYn(toYn(user.getEmailNotificationYn()));
    }

    private void requireText(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

    private String toYn(String value) {
        return value != null && value.contains("Y") ? "Y" : "N";
    }
}
