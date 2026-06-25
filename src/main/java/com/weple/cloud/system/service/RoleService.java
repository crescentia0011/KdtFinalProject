package com.weple.cloud.system.service;

import java.util.List;

public interface RoleService {
	// 목록 조회
	List<RoleVO> selectRoleList();
	
	// 단건 조회
	RoleVO selectRoleById(Long roleId);
	
	// 전체 권한 목록
	List<PermissionVO> selectPermissionList();
	
	// 역할에 매핑된 권한 코드 목록
	List<String> selectPermissionCodesByRoleid(Long roleId);
	
	// 역할 등록+권한 매핑
	int saveRole(RoleVO roleVO);
	
	// 역할 수정(권한 초기화 후 재등록)
	int updateRole(RoleVO roleVO);
	
	// 역할 삭제
	int deleteRole(Long roleId);
}
