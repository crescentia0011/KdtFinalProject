package com.weple.cloud.repository.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.weple.cloud.repository.mapper.RepositoryMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RepositoryServiceImpl implements RepositoryService {

    private static final String DEFAULT_TASK_KEYWORD = "refs,fixes,related";

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
        applyCurrentManageSetting(repository);

        // 저장소별 관리 설정을 먼저 만들고 생성된 ID를 저장소 행에 연결함
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

        // 수정 화면의 URL은 읽기 전용이라 기존 DB 값으로 고정함
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
            throw new IllegalArgumentException("삭제 확인을 위해 저장소명을 정확히 입력해주세요.");
        }

        // 외래키를 가진 저장소 행을 먼저 지우고 연결된 관리 설정을 정리함
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

    @Override
    public Map<String, String> findTaskTitles(Long projectId, List<String> taskIds) {
        if (taskIds == null || taskIds.isEmpty()) {
            return Map.of();
        }
        return repositoryMapper.selectTaskLinkInfos(projectId, taskIds).stream()
                .collect(Collectors.toMap(
                        RepositoryTaskLinkInfo::getTaskId,
                        RepositoryTaskLinkInfo::getTaskTitle,
                        (savedTitle, ignoredTitle) -> savedTitle));
    }

    @Override
    public Set<String> findProjectPermissionCodes(String userCode, Long projectId) {
        if (userCode == null || userCode.isBlank() || projectId == null) {
            return Set.of();
        }

        // 해당 프로젝트에서 사용자에게 부여된 권한 코드 목록을 한 번에 조회함
        List<String> permissionCodes = repositoryMapper.selectProjectPermissionCodes(userCode, projectId);
        if (permissionCodes == null || permissionCodes.isEmpty()) {
            return Set.of();
        }
        return permissionCodes.stream().collect(Collectors.toSet());
    }

    @Override
    @Transactional
    public void applyStoredCommitLinks(String repositoryId, List<GithubCommitInfo> commits) {
        if (repositoryId == null || repositoryId.isBlank() || commits == null || commits.isEmpty()) {
            return;
        }

        // 현재 화면에 보이는 커밋 해시만 commit_logs에서 조회함
        List<String> commitHashes = commits.stream()
                .map(GithubCommitInfo::getFullSha)
                .filter(hash -> hash != null && !hash.isBlank())
                .distinct()
                .collect(Collectors.toList());
        if (commitHashes.isEmpty()) {
            return;
        }

        Map<String, RepositoryCommitLogVO> savedCommitMap =
                repositoryMapper.selectCommitLogsByHashes(repositoryId, commitHashes).stream()
                        .collect(Collectors.toMap(
                                RepositoryCommitLogVO::getCommitHash,
                                Function.identity(),
                                (savedCommit, ignoredCommit) -> savedCommit));

        for (GithubCommitInfo commit : commits) {
            if (commit.getFullSha() == null || commit.getFullSha().isBlank()) {
                continue;
            }

            RepositoryCommitLogVO savedCommit = savedCommitMap.get(commit.getFullSha());
            if (savedCommit != null) {
                // 이미 수집된 커밋은 현재 설정이 아니라 저장 당시의 task_id를 사용함
                commit.setTaskId(toNullableText(savedCommit.getTaskId()));
                continue;
            }

            // 처음 보는 커밋만 현재 설정으로 추출된 task_id를 저장함
            repositoryMapper.mergeCommitLogIfAbsent(toCommitLog(repositoryId, commit));
        }
    }

    @Override
    public RepositoryManageSettingVO findRepositoryManageSetting(Long companyId) {
        // 회사별 저장소 설정 조회 필요함
        RepositoryManageSettingVO setting = repositoryMapper.selectRepositoryManageSetting(companyId);
        if (setting == null) {
            // 설정 행이 없으면 화면 기본값으로 임시 생성함
            setting = new RepositoryManageSettingVO();
            setting.setCompanyId(companyId);
            setting.setCommitAutoYn("Y");
            setting.setCommitTextYn("Y");
            setting.setTaskKeyword(DEFAULT_TASK_KEYWORD);
            return setting;
        }
        normalizeSettingForView(setting);
        return setting;
    }

    @Override
    @Transactional
    public void saveRepositoryManageSetting(RepositoryManageSettingVO setting) {
        // 체크박스 미전송값과 키워드 공백을 DB 저장 전 정리함
        validateAndNormalizeSetting(setting);
        if (repositoryMapper.countRepositoryManageSettings(setting.getCompanyId()) == 0) {
            // 회사에 설정 행이 아직 없으면 새로 생성함
            repositoryMapper.insertCompanyRepositoryManageSetting(setting);
            return;
        }

        // 이미 존재하는 회사 설정 행은 전체 저장소 설정에 동일하게 반영함
        repositoryMapper.updateRepositoryManageSettings(setting);
    }

    private void applyCurrentManageSetting(RepositoryVO repository) {
        // 새 저장소 등록 시 현재 회사 전역 설정을 저장소별 설정으로 복사함
        RepositoryManageSettingVO setting = findRepositoryManageSetting(repository.getCompanyId());
        repository.setCommitAutoYn(setting.getCommitAutoYn());
        repository.setCommitTextYn(setting.getCommitTextYn());
        repository.setTaskKeyword(setting.getTaskKeyword());
    }

    private void confirmAndClearExistingMainRepository(RepositoryVO repository) {
        if (!"Y".equals(repository.getMainYn())) {
            return;
        }

        RepositoryVO currentMainRepository = repositoryMapper.selectMainRepository(
                repository.getCompanyId(), repository.getProjectId());
        boolean isChangingMainRepository = currentMainRepository != null
                && !currentMainRepository.getRepositoryId().equals(repository.getRepositoryId());
        if (isChangingMainRepository && !repository.isMainChangeConfirmed()) {
            throw new IllegalStateException("이미 주 저장소가 있습니다. 변경 여부를 확인해주세요.");
        }
        if (isChangingMainRepository) {
            repositoryMapper.clearMainRepository(repository.getCompanyId(), repository.getProjectId());
        }
    }

    private void validate(RepositoryVO repository) {
        if (repository.getProjectId() == null) {
            throw new IllegalArgumentException("프로젝트 정보가 없습니다.");
        }
        requireText(repository.getRepositoryName(), "저장소명을 입력해주세요.");
        requireText(repository.getRepositoryUrl(), "GitHub 저장소 경로를 입력해주세요.");

        String repositoryUrl = repository.getRepositoryUrl().trim();
        if (!repositoryUrl.matches("^https://github\\.com/[^/\\s]+/[^/\\s]+(?:\\.git)?/?$")) {
            throw new IllegalArgumentException("GitHub 저장소 주소 형식이 올바르지 않습니다.");
        }

        repository.setRepositoryName(repository.getRepositoryName().trim());
        repository.setRepositoryUrl(repositoryUrl);
        repository.setMainYn("Y".equals(repository.getMainYn()) ? "Y" : "N");
    }

    private void validateAndNormalizeSetting(RepositoryManageSettingVO setting) {
        if (setting.getCompanyId() == null) {
            throw new IllegalArgumentException("회사 정보가 없습니다.");
        }

        // 체크박스는 꺼져 있으면 파라미터가 오지 않아서 N으로 통일함
        setting.setCommitAutoYn("Y".equals(setting.getCommitAutoYn()) ? "Y" : "N");
        setting.setCommitTextYn("Y".equals(setting.getCommitTextYn()) ? "Y" : "N");

        // 쉼표 앞뒤 공백과 빈 키워드는 제거함
        setting.setTaskKeyword(normalizeKeywords(setting.getTaskKeyword()));
        if ("Y".equals(setting.getCommitTextYn()) && setting.getTaskKeyword().isBlank()) {
            throw new IllegalArgumentException("텍스트 형식을 적용하려면 참조 키워드를 최소 1개 입력해주세요.");
        }

        // 텍스트 형식을 꺼도 다음 재설정에 쓸 기본 키워드는 유지함
        if (setting.getTaskKeyword().isBlank()) {
            setting.setTaskKeyword(DEFAULT_TASK_KEYWORD);
        }
    }

    private void normalizeSettingForView(RepositoryManageSettingVO setting) {
        // DB 값이 비어 있거나 이상해도 화면에서는 안정적인 Y/N으로 보여줌
        setting.setCommitAutoYn("N".equals(setting.getCommitAutoYn()) ? "N" : "Y");
        setting.setCommitTextYn("N".equals(setting.getCommitTextYn()) ? "N" : "Y");
        String keywords = normalizeKeywords(setting.getTaskKeyword());
        setting.setTaskKeyword(keywords.isBlank() ? DEFAULT_TASK_KEYWORD : keywords);
    }

    private String normalizeKeywords(String keywords) {
        if (keywords == null) {
            return "";
        }

        // 중복 키워드는 한 번만 저장해서 매칭 기준이 흔들리지 않게 함
        return java.util.Arrays.stream(keywords.split(","))
                .map(String::trim)
                .filter(keyword -> !keyword.isEmpty())
                .distinct()
                .collect(Collectors.joining(","));
    }

    private RepositoryCommitLogVO toCommitLog(String repositoryId, GithubCommitInfo commit) {
        RepositoryCommitLogVO commitLog = new RepositoryCommitLogVO();
        commitLog.setCommitId(createCommitId(repositoryId, commit.getFullSha()));
        commitLog.setRepositoryId(repositoryId);
        commitLog.setCommitHash(commit.getFullSha());
        commitLog.setCommitUser(commit.getAuthorEmail());
        commitLog.setCommitMessage(commit.getMessage());
        commitLog.setCommittedAt(commit.getCommittedAt());
        commitLog.setTaskId(toNullableText(commit.getTaskId()));
        return commitLog;
    }

    private String createCommitId(String repositoryId, String fullSha) {
        String prefix = repositoryId + "-";
        int hashLength = 50 - prefix.length();
        if (hashLength <= 0) {
            return repositoryId.substring(0, Math.min(repositoryId.length(), 50));
        }

        // commit_id 길이 제한 안에서 해시를 최대한 길게 붙여 충돌 가능성을 줄임
        return prefix + fullSha.substring(0, Math.min(fullSha.length(), hashLength));
    }

    private String toNullableText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value;
    }

    private void requireText(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }
}
