package com.weple.cloud.project.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.weple.cloud.history.worklog.service.WorkLogVO;
import com.weple.cloud.project.mapper.ProjectWorkLogMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProjectWorkLogServiceImpl implements ProjectWorkLogService {
	private final ProjectWorkLogMapper projectWorkLogMapper;
	
	@Override
	public List<WorkLogVO> findAll(
			String projectId,
            String startDate,
            String endDate,
            String userCode,
            List<String> typeNames){
		
		return projectWorkLogMapper.selectProjectWorkLog(
                projectId,
                startDate,
                endDate,
                userCode,
                typeNames);
	}
}
