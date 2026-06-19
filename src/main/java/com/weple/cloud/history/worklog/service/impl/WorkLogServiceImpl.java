package com.weple.cloud.history.worklog.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.weple.cloud.history.worklog.mapper.WorkLogMapper;
import com.weple.cloud.history.worklog.service.WorkLogService;
import com.weple.cloud.history.worklog.service.WorkLogVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorkLogServiceImpl implements WorkLogService {
	private final WorkLogMapper workLogMapper;
	
	@Override
	public List<WorkLogVO> findAll(){
		return workLogMapper.selectAll();
	}
}
