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
        if (repositoryMapper.countRepositoryByNameOrUrl(
                repository.getCompanyId(), repository.getProjectId(),
                repository.getRepositoryName(), repository.getRepositoryUrl()) > 0) {
            throw new IllegalArgumentException("이미 등록된 저장소명 또는 GitHub 주소입니다.");
        }

        confirmAndClearExistingMainRepository(repository);

        // 저장소별 기본 관리 설정을 먼저 만들어 외래 키로 연결합니다.
        if (repositoryMapper.insertRepositoryManageSetting(repository) != 1) {
            throw new IllegalStateException("저장소 관리 설정 생성 중 오류가 발생했습니다.");
        }
        if (repositoryMapper.insertRepository(repository) != 1) {
            throw new IllegalStateException("저장소 등록 중 오류가 발생했습니다.");
        }
    }

    @Override
    @Transactional
    public void updateRepository(RepositoryVO repository) {
        RepositoryVO savedRepository = repositoryMapper.selectRepository(
                repository.getCompanyId(), repository.getProjectId(), repository.getRepositoryId());
        if (savedRepository == null) {
            throw new IllegalArgumentException("수정할 저장소를 찾을 수 없습니다.");
        }

        // 수정 화면의 URL은 읽기 전용이며, 변조된 요청도 기존 DB 값으로 차단합니다.
        repository.setRepositoryUrl(savedRepository.getRepositoryUrl());
        validate(repository);
        if (repositoryMapper.countRepositoryNameExcept(
                repository.getCompanyId(), repository.getProjectId(),
                repository.getRepositoryName(), repository.getRepositoryId()) > 0) {
            throw new IllegalArgumentException("이미 사용 중인 저장소명입니다.");
        }

        confirmAndClearExistingMainRepository(repository);
        if (repositoryMapper.updateRepository(repository) != 1) {
            throw new IllegalStateException("저장소 수정 중 오류가 발생했습니다.");
        }
    }

    @Override
    @Transactional
    public void deleteRepository(Long companyId, Long projectId, String repositoryId, String confirmationName) {
        RepositoryVO savedRepository = repositoryMapper.selectRepository(companyId, projectId, repositoryId);
        if (savedRepository == null) {
            throw new IllegalArgumentException("삭제할 저장소를 찾을 수 없습니다.");
        }
        if (!savedRepository.getRepositoryName().equals(confirmationName == null ? "" : confirmationName.trim())) {
            throw new IllegalArgumentException("삭제 확인을 위해 저장소명을 정확히 입력해 주세요.");
        }

        // 외래 키를 가진 저장소 행을 먼저 지운 뒤 연결된 관리 설정을 정리합니다.
        if (repositoryMapper.deleteRepository(companyId, projectId, repositoryId) != 1) {
            throw new IllegalStateException("저장소 삭제 중 오류가 발생했습니다.");
        }
        repositoryMapper.deleteRepositoryManageSetting(savedRepository.getRepositoryManageId());
    }

    @Override
    public boolean hasRepository(Long projectId) {
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

    @Override
    public RepositoryVO findMainRepository(Long companyId, Long projectId) {
        return repositoryMapper.selectMainRepository(companyId, projectId);
    }

    // 다른 저장소가 이미 주 저장소라면 사용자의 명시적 확인 후에만 교체합니다.
    private void confirmAndClearExistingMainRepository(RepositoryVO repository) {
        if (!"Y".equals(repository.getMainYn())) {
            return;
        }

        RepositoryVO currentMainRepository = repositoryMapper.selectMainRepository(
                repository.getCompanyId(), repository.getProjectId());
        boolean isChangingMainRepository = currentMainRepository != null
                && !currentMainRepository.getRepositoryId().equals(repository.getRepositoryId());
        if (isChangingMainRepository && !repository.isMainChangeConfirmed()) {
            throw new IllegalStateException("이미 주 저장소가 있습니다. 변경 여부를 확인해 주세요.");
        }
        if (isChangingMainRepository) {
            repositoryMapper.clearMainRepository(repository.getCompanyId(), repository.getProjectId());
        }
    }

    private void validate(RepositoryVO repository) {
        if (repository.getProjectId() == null) {
            throw new IllegalArgumentException("프로젝트 정보가 없습니다.");
        }
        requireText(repository.getRepositoryName(), "저장소명을 입력해 주세요.");
        requireText(repository.getRepositoryUrl(), "GitHub 저장소 경로를 입력해 주세요.");

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
