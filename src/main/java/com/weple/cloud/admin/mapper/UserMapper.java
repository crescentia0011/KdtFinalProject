package com.weple.cloud.admin.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.weple.cloud.admin.service.UserVO;

@Mapper
public interface UserMapper {
	// 전체 작업내역 - 활성 사용자 전체 목록
	List<UserVO> selectAllActiveUsers();
	
	// 프로젝트 내 작업내역 - 해당 프로젝트 구성원(사용자) 목록
	List<UserVO> selectUsersByProjectId(@Param("projectId") String projectId);
}
