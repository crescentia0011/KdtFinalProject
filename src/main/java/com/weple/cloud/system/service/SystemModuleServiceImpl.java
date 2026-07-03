package com.weple.cloud.system.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.weple.cloud.system.mapper.SystemModuleMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SystemModuleServiceImpl implements SystemModuleService{

	private final SystemModuleMapper systemModuleMapper;
	
	@Override
	public List<SystemModuleVO> findModuleAll() {
		return systemModuleMapper.findModuleAll();
	}

	@Override
    public List<String> findEnabledModuleCodes(Long companyId) {
        return systemModuleMapper.findEnabledModuleCodes(companyId);
    }

    @Override
    @Transactional
    public void saveEnabledModules(Long companyId, List<String> moduleCodes) {
        systemModuleMapper.deleteModulesByCompany(companyId);

        if (moduleCodes == null) return;

        // 체크된 것만 새로 저장
        for (String code : moduleCodes) {
            String moduleId = companyId + "_" + code;
            systemModuleMapper.insertModule(moduleId, code, companyId);
        }
    }

    @Override
    public List<TaskTypeVO> findTaskTypeAll(Long companyId) {
        return systemModuleMapper.findTaskTypeAll(companyId);
    }

    @Override
    public List<String> findEnabledTaskTypeIds(Long companyId) {
        return systemModuleMapper.findEnabledTaskTypeIds(companyId);
    }

    @Override
    @Transactional
    public void saveEnabledTaskTypes(Long companyId, List<String> taskTypeIds) {
        systemModuleMapper.resetTaskTypeEnabled(companyId);

        if (taskTypeIds == null || taskTypeIds.isEmpty()) return;

        systemModuleMapper.enableTaskTypes(companyId, new java.util.ArrayList<>(new java.util.LinkedHashSet<>(taskTypeIds)));
    }  
	
}