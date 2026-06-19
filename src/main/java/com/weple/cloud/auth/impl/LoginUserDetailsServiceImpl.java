package com.weple.cloud.auth.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.weple.cloud.auth.mapper.LoginMapper;
import com.weple.cloud.auth.service.LoginUserDetails;
import com.weple.cloud.auth.service.LoginUserVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoginUserDetailsServiceImpl implements UserDetailsService {

	// 로그인 사용자/권한 조회 Mapper
	private final LoginMapper loginMapper;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		// 로그인 아이디로 사용자 정보 조회
		LoginUserVO loginUser = loginMapper.selectLoginUserByLoginId(username);
		if(loginUser == null) {
			throw new UsernameNotFoundException("아이디 또는 비밀번호가 일치하지 않습니다.");
		}
		if("a1".equals(loginUser.getStatus())) {
			throw new DisabledException("가입 승인 대기중입니다.");
		}
		if("a3".equals(loginUser.getStatus())) {
			throw new DisabledException("비활성화된 계정입니다.");
		}
		// 시큐리티 권한 생성
		List<SimpleGrantedAuthority> authorities = new ArrayList<>();
		// 모든 로그인 사용자에게 기본 role 부여
		authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
		// 기업 최초 관리자에게 권한 부여
		if (Integer.valueOf(1).equals(loginUser.getOwnerYn())) {
		    authorities.add(new SimpleGrantedAuthority("ROLE_COMPANY_OWNER"));
		}

		// 부여받은 관리자 권한 부여
		if (Integer.valueOf(1).equals(loginUser.getAdminYn())) {
		    authorities.add(new SimpleGrantedAuthority("ROLE_COMPANY_ADMIN"));
		}
		// DB 세부 권한 조회
		List<String> permissionCodes = loginMapper.selectPermissionCodesByUserCode(loginUser.getUserCode());
		
		// 세부 권한 코드를 스프링 시큐리티 권한으로 변환
		if (permissionCodes != null) {
		    for (String permissionCode : permissionCodes) {
		        if (permissionCode != null && !permissionCode.isBlank()) {	// null로 들어올 시 방어 코드
		            authorities.add(new SimpleGrantedAuthority(permissionCode));
		        }
		    }
		}
		
		// 스프링 시큐리티 인증 사용자 객체 반환
		return new LoginUserDetails(loginUser, authorities);
	}
}