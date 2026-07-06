package com.weple.cloud.project.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.weple.cloud.project.mapper.ProjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {
	
	private final ProjectMapper projectMapper;
	
	// 전체 목록 조회(페이징)
	@Override
	public List<ProjectVO> findAll(String keyword, int offset, int pageSize){
		return projectMapper.selectAll(keyword, offset, pageSize);
	}
	
	// 전체 목록 조회(페이징x)
	@Override
	public List<ProjectVO> findAll(String keyword) {
		return projectMapper.selectAllNoPage(keyword);
	}
	
	// 전체 건수 조회
	@Override
	public int countAll(String keyword) {
		return projectMapper.countAll(keyword);
	}
	
	// 단건 조회
	@Override
	public ProjectVO findById(String projectId) {
		return projectMapper.selectById(projectId);
	}

	// 관리에서 선택된 모듈 전체 목록 조회
	@Override
	public List<String> findModuleNames(Long projectId) {
		return projectMapper.selectModuleNames(projectId);
	}
	
	// 네비바 활성화된 모듈만 조회
	@Override
	public List<String> findActiveModuleNames(Long projectId) {
		return projectMapper.selectActiveModuleNames(projectId);
	}
	
	// 설정 페이지 - 프로젝트 설정 정보 조회
	@Override
	public ProjectVO findSettingById(Long projectId) {
		ProjectVO vo = projectMapper.selectSettingById(projectId);
		List<String> modules = projectMapper.selectModuleNames(projectId);
		vo.setModuleNames(modules != null ? modules : new ArrayList<>());
		return vo;
	}
	
	// 설정 페이지 - 프로젝트 설정 저장
    @Override
    @Transactional
    public void saveProjectSetting(ProjectVO vo) {
        // 기본 정보 수정(제목, 설명)
        projectMapper.updateProjectSetting(vo);

        List<String> checkedModules = vo.getModuleNames();
        if (checkedModules == null) checkedModules = new ArrayList<>();

        // 이미 module_mapping에 등록되어 있는 모듈(관리-프로젝트에서 선택했던 이력)
        List<String> existingModules = projectMapper.selectModuleNames(vo.getProjectId());
        Set<String> existingSet = new HashSet<>(existingModules);

        // 기존에 있던 모듈은 is_active만 갱신
        for (String moduleName : existingModules) {
            String isActive = checkedModules.contains(moduleName) ? "Y" : "N";
            projectMapper.updateModuleActive(vo.getProjectId(), moduleName, isActive);
        }

        // 이번에 새로 체크됐지만 아직 module_mapping에 없던 모듈은 새로 등록 (is_active는 기본값 Y로 들어감)
        for (String moduleName : checkedModules) {
            if (!existingSet.contains(moduleName)) {
                projectMapper.insertModuleMapping(vo.getProjectId(), moduleName);
            }
        }
    }
	
	// URL 접근 제어 - module_mapping row 존재 여부로 활성화 판단
    @Override
    public boolean isModuleActive(Long projectId, String moduleName) {
        return projectMapper.isModuleActive(projectId, moduleName) > 0;
    }

	@Override
	public Set<String> findProjectPermissionCodes(String userCode, Long projectId) {
		return new java.util.HashSet<>(projectMapper.selectProjectPermissionCodes(userCode, projectId));
	}

	@Override
	public boolean isMember(String userCode, Long projectId) {
		return projectMapper.isMember(userCode, projectId);
	}

	@Override
	public List<ProjectVO> findAllByMember(String userCode, String keyword, int offset, int pageSize) {
		return projectMapper.selectAllByMember(userCode, keyword, offset, pageSize);
	}

	@Override
	public int countAllByMember(String userCode, String keyword) {
		return projectMapper.countAllByMember(userCode, keyword);
	}

	// 관리자용 - 소속 회사 전체 프로젝트 조회 (구성원 여부 무관)
	@Override
	public List<ProjectVO> findAllByCompany(String companyId, String keyword, int offset, int pageSize) {
		return projectMapper.selectAllByCompany(companyId, keyword, offset, pageSize);
	}

	@Override
	public int countAllByCompany(String companyId, String keyword) {
		return projectMapper.countAllByCompany(companyId, keyword);
	}

	@Override
	public Set<String> findAnyProjectPermissionCodes(String userCode) {
		return new HashSet<>(projectMapper.selectAnyProjectPermissionCodes(userCode));
	}

}