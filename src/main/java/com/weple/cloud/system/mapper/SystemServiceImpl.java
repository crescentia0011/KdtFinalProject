package com.weple.cloud.system.mapper;

import java.util.List;

import org.springframework.stereotype.Service;

import com.weple.cloud.system.service.SystemService;
import com.weple.cloud.system.service.TaskTypeVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
// =---------------일감유형-------------
public class SystemServiceImpl implements SystemService {

	private final SystemMapper systemMapper;
	@Override
	public List<TaskTypeVO> findAll() {
		return systemMapper.selectTaskTypeAll();
	}

	@Override
	public int addTaskType(TaskTypeVO taskTypeVO) {
		int result = systemMapper.insertTaskType(taskTypeVO);
		return result == 1 ? taskTypeVO.getTypeId() : -1;
	}

}
