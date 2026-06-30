package com.weple.cloud.system.service;

import java.util.List;

public interface SystemModuleService {
	public List<SystemModuleVO> findModuleAll();
	
	public List<String> findEnabledModuleCodes(Long companyId);
    public void saveEnabledModules(Long companyId, List<String> moduleCodes);
    
    public List<TaskTypeVO> findTaskTypeAll(Long companyId);
    public List<String> findEnabledTaskTypeIds(Long companyId);
    public void saveEnabledTaskTypes(Long companyId, List<String> taskTypeIds);
}
