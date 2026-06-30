package com.weple.cloud.system.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.weple.cloud.system.service.SystemModuleVO;
import com.weple.cloud.system.service.TaskTypeVO;

public interface SystemModuleMapper {
	public List<SystemModuleVO> findModuleAll();
	
	public List<String> findEnabledModuleCodes(Long companyId);
    long deleteModulesByCompany(Long companyId);
    long insertModule(@Param("moduleId") String moduleId, @Param("moduleName") String moduleName, @Param("companyId") Long companyId);
    
    List<TaskTypeVO> findTaskTypeAll(Long companyId);
    List<String> findEnabledTaskTypeIds(Long companyId);
    long deleteTaskTypesByCompany(Long companyId);
    long insertTaskType(@Param("typeId") String typeId, @Param("typeName") String typeName, @Param("companyId") Long companyId);
}
