package com.weple.cloud.auth.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.weple.cloud.auth.service.SignupRequestVO;

@Mapper
public interface SignupMapper {

    // 회사 코드로 가입 대상 회사 ID를 조회합니다.
    Long selectCompanyIdByCompanyCode(@Param("companyCode") String companyCode);

    // 같은 로그인 아이디가 이미 존재하는지 확인합니다.
    int countUserByLoginId(@Param("loginId") String loginId);

    // 검증이 끝난 회원가입 정보를 USERS 테이블에 저장합니다.
    int insertSignupUser(SignupRequestVO request);
}
