package com.weple.cloud.repository.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.weple.cloud.repository.mapper.RepositoryMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RepositoryServiceImpl implements RepositoryService {

    private final RepositoryMapper repositoryMapper;

    @Override
    @Transactional
    public void registerRepository(RepositoryVO repository) {
        validate(repository);

        // 같은 GitHub 저장소를 여러 번 등록하지 않도록 DB 저장 전에 확인합니다.
        if (repositoryMapper.countRepositoryByNameOrUrl(
                repository.getRepositoryName(), repository.getRepositoryUrl()) > 0) {
            throw new IllegalArgumentException("이미 등록된 저장소명 또는 GitHub 주소입니다.");
        }

        // 프로젝트별 주 저장소는 하나만 유지합니다.
        if ("Y".equals(repository.getMainYn())) {
            repositoryMapper.clearMainRepository(repository.getCompanyId(), repository.getProjectId());
        }

        // 커밋 자동 처리 등 저장소별 기본 관리 설정을 생성합니다.
        if (repositoryMapper.insertRepositoryManageSetting(repository) != 1) {
            throw new IllegalStateException("저장소 관리 설정 생성 중 오류가 발생했습니다.");
        }

        if (repositoryMapper.insertRepository(repository) != 1) {
            throw new IllegalStateException("저장소 등록 중 오류가 발생했습니다.");
        }
    }

    @Override
    public boolean hasRepository(Long projectId) {
        // 화면 탭 노출에는 목록 전체가 아닌 존재 여부만 필요합니다.
        return repositoryMapper.countRepositoriesByProjectId(projectId) > 0;
    }

    @Override
    public List<RepositoryVO> findRepositories(Long companyId, Long projectId) {
        return repositoryMapper.selectRepositories(companyId, projectId);
    }

    @Override
    public RepositoryVO findRepository(Long companyId, Long projectId, String repositoryId) {
        return repositoryMapper.selectRepository(companyId, projectId, repositoryId);
    }

    private void validate(RepositoryVO repository) {
        // 프로젝트 정보와 필수 입력값은 DB 저장 전에 먼저 검증합니다.
        if (repository.getProjectId() == null) {
            throw new IllegalArgumentException("프로젝트 정보가 없습니다.");
        }
        requireText(repository.getRepositoryName(), "저장소명을 입력해주세요.");
        requireText(repository.getRepositoryUrl(), "GitHub 저장소 경로를 입력해주세요.");

        // GitHub owner/repository 형식과 .git 접미사 유무를 함께 허용합니다.
        String repositoryUrl = repository.getRepositoryUrl().trim();
        if (!repositoryUrl.matches("^https://github\\.com/[^/\\s]+/[^/\\s]+(?:\\.git)?/?$")) {
            throw new IllegalArgumentException("GitHub 저장소 주소 형식이 올바르지 않습니다.");
        }

        repository.setRepositoryName(repository.getRepositoryName().trim());
        repository.setRepositoryUrl(repositoryUrl);
        repository.setMainYn("Y".equals(repository.getMainYn()) ? "Y" : "N");
    }

    private void requireText(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }
}
