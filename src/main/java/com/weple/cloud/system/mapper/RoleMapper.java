package com.weple.cloud.system.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.weple.cloud.system.service.PermissionVO;
import com.weple.cloud.system.service.RoleVO;

@Mapper
public interface RoleMapper {
	// 전체 목록 조회
	List<RoleVO> selectRoleList();
	
	// 단건 조회
	RoleVO selectRoleById(Long roleId);
	
	// 전체 권한 목록 조회
	List<PermissionVO> selectPermissionList();
	
	// 역할에 매핑된 권한 코드 목록
	List<String> selectPermissionCodesByRoleId(Long roleId);
	
	// 역할 등록
	int insertRole(RoleVO roleVo);
	
	// 역할-권한 매핑 등록
	int insertRolePermission(
			@Param("roleId") Long roleId,
			@Param("permissionCode") String permissionCode
			);
	
	// 역할 이름 수정
	int updateRole(RoleVO roleVO);
	
	// 역할-권한 매핑 전체 삭제(수정 시 초기화)
	int deleteRolePermissions(Long roleId);
	
	// 역할 삭제
	int deleteRole(Long roleId);
}
