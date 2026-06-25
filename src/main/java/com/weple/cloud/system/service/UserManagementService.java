package com.weple.cloud.system.service;

import java.util.List;

public interface UserManagementService {

    // 현재 관리자가 속한 회사의 활성·비활성 사용자를 요청한 페이지 범위만 조회
    List<UserManagementVO> findUsers(Long companyId, String keyword, int offset, int pageSize);

    // 사용자 관리 목록의 전체 페이지 수 계산에 사용할 사용자 수를 조회
    int countUsers(Long companyId, String keyword);

    // 기업최고관리자만 부여받은 관리자의 상태를 변경할 수 있도록 사용자 상태를 변경
    void changeUserStatus(Long companyId, int actorOwnerYn, String userCode, String status);

    // 관리자가 입력한 신규 사용자 정보를 검증한 뒤 같은 회사의 활성 사용자로 등록
    void createUser(Long companyId, int actorOwnerYn, UserManagementCreateVO user);
}
