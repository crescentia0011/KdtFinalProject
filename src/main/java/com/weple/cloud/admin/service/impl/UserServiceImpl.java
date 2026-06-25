package com.weple.cloud.admin.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.weple.cloud.admin.mapper.UserMapper;
import com.weple.cloud.admin.service.UserService;
import com.weple.cloud.admin.service.UserVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
	private final UserMapper userMapper;

	@Override
	public List<UserVO> findAllActiveUsers() {
		return userMapper.selectAllActiveUsers();
	}

	@Override
	public List<UserVO> findUsersByProjectId(String projectId) {
		return userMapper.selectUsersByProjectId(projectId);
	}

}
