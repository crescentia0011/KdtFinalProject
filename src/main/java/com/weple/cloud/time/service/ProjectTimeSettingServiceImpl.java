package com.weple.cloud.time.service;

import java.util.LinkedHashSet;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.weple.cloud.time.mapper.ProjectTimeSettingMapper;
import com.weple.cloud.system.service.CodeValueVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProjectTimeSettingServiceImpl implements ProjectTimeSettingService {

    private final ProjectTimeSettingMapper mapper;

    @Override
    public List<CodeValueVO> findClassificationOptions(Long companyId) {
        return mapper.findUsingWorkClassifications(companyId);
    }

    @Override
    public List<String> findSelectedClassificationIds(Long projectId) {
        return mapper.findSelectedClassificationIds(projectId);
    }

    @Override
    @Transactional
    public void saveSelectedClassifications(Long projectId, List<String> taskClassificationIds) {
        mapper.deleteSelectionByProject(projectId);

        if (taskClassificationIds == null) return;

        for (String id : new LinkedHashSet<>(taskClassificationIds)) {
            mapper.insertSelection(projectId, id);
        }
    }
}