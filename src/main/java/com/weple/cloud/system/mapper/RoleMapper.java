package com.weple.cloud.system.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.weple.cloud.system.service.PermissionVO;
import com.weple.cloud.system.service.RoleVO;

@Mapper
public interface RoleMapper {
	// 전체 목록 조회
	List<RoleVO> selectRoleList(@Param("companyId") Long companyId);
	
	// 단건 조회
	RoleVO selectRoleById(@Param("roleId") Long roleId, @Param("companyId") Long companyId);
	
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
	int deleteRole(@Param("roleId") Long roleId, @Param("companyId") Long companyId);
	
	// 역할이 구성원에게 할당되어 사용 중인지 확인 (member_roles 참조 건수)
	int countMemberRolesByRoleId(@Param("roleId") Long roleId);
	
	Long selectRoleIdByName(@Param("companyId") Long companyId, @Param("roleName") String roleName);
}