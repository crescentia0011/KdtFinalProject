package com.weple.cloud.system.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.weple.cloud.system.mapper.RoleMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
	private final RoleMapper roleMapper;
	
	// 목록 조회
	@Override
	public List<RoleVO> selectRoleList() {
		return roleMapper.selectRoleList();
	}
	
	// 단건 조회
	@Override
	public RoleVO selectRoleById(Long roleId) {
		return roleMapper.selectRoleById(roleId);
	}
	
	// 전체 권한 목록
	@Override
	public List<PermissionVO> selectPermissionList() {
		return roleMapper.selectPermissionList();
	}
	
	//역할에 매핑된 권한 코드
	@Override
	public List<String> selectPermissionCodesByRoleid(Long roleId) {
		return roleMapper.selectPermissionCodesByRoleId(roleId);
	}
	
	// 역할 등록+권한 매핑
	@Override
	@Transactional
	public int saveRole(RoleVO roleVO) {
		int result = roleMapper.insertRole(roleVO);
		if(result > 0 && roleVO.getPermissionCodes() != null) {
			for(String code : roleVO.getPermissionCodes()) {
				roleMapper.insertRolePermission(roleVO.getRoleId(), code);
			}
		}
		return result;
	}
	
	// 역할 수정(권한 초기화 후 재등록)
	@Override
	@Transactional
	public int updateRole(RoleVO roleVO) {
		int result = roleMapper.updateRole(roleVO);
		roleMapper.deleteRolePermissions(roleVO.getRoleId());
		if(roleVO.getPermissionCodes() != null) {
			for(String code : roleVO.getPermissionCodes()) {
				roleMapper.insertRolePermission(roleVO.getRoleId(), code);
			}
		}
		return 1;
	}

	// 역할 삭제
	@Override
	@Transactional
	public int deleteRole(Long roleId) {
		roleMapper.deleteRolePermissions(roleId); // 매핑 먼저 삭제
		return roleMapper.deleteRole(roleId);
	}
}
