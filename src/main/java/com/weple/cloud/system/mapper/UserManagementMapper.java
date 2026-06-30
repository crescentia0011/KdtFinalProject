package com.weple.cloud.system.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.weple.cloud.system.service.UserManagementCreateVO;
import com.weple.cloud.system.service.UserManagementProjectVO;
import com.weple.cloud.system.service.UserManagementUpdateVO;
import com.weple.cloud.system.service.UserManagementVO;

@Mapper
public interface UserManagementMapper {

    // 가입승인 완료 후 활성 또는 비활성 상태인 같은 회사 사용자를 요청한 페이지 범위만 조회합니다.
    List<UserManagementVO> selectUserManagementList(@Param("companyId") Long companyId,
                                                     @Param("keyword") String keyword,
                                                     @Param("offset") int offset,
                                                     @Param("pageSize") int pageSize);

    // 사용자 관리 목록의 전체 페이지 수 계산에 사용할 사용자 수를 조회합니다.
    int countUserManagementList(@Param("companyId") Long companyId,
                                @Param("keyword") String keyword);

    // 사용자 상세조회 화면에 표시할 기본 정보와 소속 그룹 정보를 조회합니다.
    UserManagementVO selectUserDetail(@Param("companyId") Long companyId,
                                      @Param("userCode") String userCode);

    // 사용자 상세조회 화면에 표시할 프로젝트별 역할 목록을 조회합니다.
    List<UserManagementProjectVO> selectUserProjects(@Param("companyId") Long companyId,
                                                     @Param("userCode") String userCode);

    // 같은 회사에 속한 활성 회원을 활성(a2) 또는 비활성(a3) 상태로 변경
    int updateUserStatus(@Param("companyId") Long companyId,
                         @Param("actorOwnerYn") int actorOwnerYn,
                         @Param("userCode") String userCode,
                         @Param("status") String status);

    // 신규 사용자 등록 전 같은 로그인 아이디가 이미 사용 중인지 확인합니다.
    int countUserByLoginId(@Param("loginId") String loginId);

    // 신규 사용자 등록 전 같은 이메일이 이미 사용 중인지 확인합니다.
    int countUserByEmail(@Param("email") String email);

    // 수정 대상 자신을 제외하고 같은 로그인 아이디가 이미 사용 중인지 확인합니다.
    int countUserByLoginIdExcept(@Param("loginId") String loginId,
                                 @Param("userCode") String userCode);

    // 수정 대상 자신을 제외하고 같은 이메일이 이미 사용 중인지 확인합니다.
    int countUserByEmailExcept(@Param("email") String email,
                               @Param("userCode") String userCode);

    // 수정 화면에서 선택한 그룹이 같은 회사에 존재하는지 확인합니다.
    int countGroupByCompany(@Param("companyId") Long companyId,
                            @Param("groupId") Integer groupId);

    // 기업 관리자 화면에서 입력한 신규 사용자를 같은 회사의 활성 사용자로 등록합니다.
    int insertUser(UserManagementCreateVO user);

    // 사용자 기본정보 수정 화면에서 허용한 아이디, 이름, 이메일, 연락처만 변경합니다.
    int updateUserBasicInfo(@Param("companyId") Long companyId,
                            @Param("actorOwnerYn") int actorOwnerYn,
                            @Param("user") UserManagementUpdateVO user);
}
