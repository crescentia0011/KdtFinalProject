package com.weple.cloud.system.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.weple.cloud.system.mapper.SystemProjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SystemProjectServiceImpl implements SystemProjectService {
	private final SystemProjectMapper systemProjectMapper;
	
	@Override
	@Transactional
	public int createProject(SystemProjectVO projectVO) {
		int result = systemProjectMapper.insertInfo(projectVO);
		
		// 프로젝트 등록 성공 시 선택된 모듈 리스트 매핑 등록
		if(result > 0) {
			List<String> moduleNames = projectVO.getModuleNames();
			if(moduleNames!= null && !moduleNames.isEmpty()) {
				systemProjectMapper.insertModuleMapping(projectVO);
			}
		}
		return result;
	}

	@Override
	public boolean existsByIdentifier(String projectIdentifier) {
		return systemProjectMapper.countByIdentifier(projectIdentifier) > 0;
	}

	@Override
	@Transactional
	public int deleteProject(String projectId) {
		return systemProjectMapper.deleteProject(projectId);
	}

	@Override
	public List<SystemProjectVO> selectProjectList(SystemProjectVO vo) {
		vo.setOffset((vo.getPage() - 1) * vo.getPageSize());
	    return systemProjectMapper.selectProjectList(vo);
	}

	@Override
	public int selectProjectCount(SystemProjectVO vo) {
		return systemProjectMapper.selectProjectCount(vo);
	}

	@Override
	public SystemProjectVO selectProjectById(Long projectId) {
		SystemProjectVO project = systemProjectMapper.selectProjectById(projectId);
		
		if(project != null) {
			project.setModuleNames(systemProjectMapper.selectModuleNames(projectId));
		}
		
		return project;
	}

	@Override
	@Transactional
	public int updateProject(SystemProjectVO projectVO) {
		int result = systemProjectMapper.updateProject(projectVO);
		
		if(result > 0) {
			// 기존 모듈 매핑 삭제 후 재등록
			systemProjectMapper.deleteModuleMapping(projectVO.getProjectId());
			
			List<String> moduleNames = projectVO.getModuleNames();
			if(moduleNames!= null && !moduleNames.isEmpty()) {
				systemProjectMapper.insertModuleMapping(projectVO);
			}
		}
		return result;
	}
}
