package com.weple.cloud.repository.service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service

public class GithubRepositoryReader {

    private static final String GITHUB_API = "https://api.github.com/repos/";
    
    // 환경설정에서 토큰 받아오기
    @Value("${github.api.token}")
    private String githubToken;
    
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 공개 GitHub 저장소에서 상세 정보, 브랜치, 최근 커밋을 가져옵니다.
    public GithubRepositoryInfo readRepository(String repositoryUrl, String requestedBranch,
                                               String requestedDirectory, String requestedFilePath) {
        String repositoryPath = toRepositoryPath(repositoryUrl);
        JsonNode repository = getJson(GITHUB_API + repositoryPath);

        GithubRepositoryInfo info = new GithubRepositoryInfo();
        info.setDescription(repository.path("description").asText("저장소 설명이 없습니다."));
        info.setDefaultBranch(repository.path("default_branch").asText("main"));
        info.setSelectedBranch(requestedBranch == null || requestedBranch.isBlank()
                ? info.getDefaultBranch() : requestedBranch);
        info.setCurrentDirectory(requestedDirectory == null ? "" : requestedDirectory);
        info.setParentDirectory(parentDirectory(info.getCurrentDirectory()));

        // 서로 의존하지 않는 GitHub 조회는 병렬로 실행해 상세 화면 대기 시간을 줄입니다.
        CompletableFuture<List<String>> branchesFuture = CompletableFuture.supplyAsync(
                () -> readBranchesOrDefault(repositoryPath, info.getDefaultBranch()));
        CompletableFuture<List<GithubFileInfo>> filesFuture = CompletableFuture.supplyAsync(
                () -> readFilesOrEmpty(repositoryPath, info.getSelectedBranch(), info.getCurrentDirectory()));
        CompletableFuture<List<GithubCommitInfo>> commitsFuture = CompletableFuture.supplyAsync(
                () -> readCommitsOrEmpty(repositoryPath, info.getSelectedBranch()));

        info.setBranches(branchesFuture.join());
        info.setFiles(filesFuture.join());
        info.setCommits(commitsFuture.join());

        // 파일 본문은 사용자가 선택했을 때만 요청해 초기 진입을 가볍게 합니다.
        if (requestedFilePath == null || requestedFilePath.isBlank()) {
            info.setSelectedFileContent("왼쪽 목록에서 파일을 선택하세요.");
        } else {
            setSelectedFileContent(info, repositoryPath, requestedFilePath);
        }
        return info;
    }

    private List<String> readBranchesOrDefault(String repositoryPath, String defaultBranch) {
        try {
            return readBranches(repositoryPath);
        } catch (IllegalStateException ex) {
            return List.of(defaultBranch);
        }
    }

    private List<GithubFileInfo> readFilesOrEmpty(String repositoryPath, String branch, String directory) {
        try {
            return readFiles(repositoryPath, branch, directory);
        } catch (IllegalStateException ex) {
            return List.of();
        }
    }

    private List<GithubCommitInfo> readCommitsOrEmpty(String repositoryPath, String branch) {
        try {
            return readCommits(repositoryPath, branch);
        } catch (IllegalStateException ex) {
            return List.of();
        }
    }

    private List<String> readBranches(String repositoryPath) {
        JsonNode branches = getJson(GITHUB_API + repositoryPath + "/branches?per_page=100");
        List<String> branchNames = new ArrayList<>();
        for (JsonNode branch : branches) {
            branchNames.add(branch.path("name").asText());
        }
        return branchNames;
    }

    private List<GithubCommitInfo> readCommits(String repositoryPath, String branch) {
        JsonNode commits = getJson(GITHUB_API + repositoryPath + "/commits?sha=" + encodeQueryValue(branch) + "&per_page=10");
        List<GithubCommitInfo> commitInfos = new ArrayList<>();
        for (JsonNode commit : commits) {
            GithubCommitInfo commitInfo = new GithubCommitInfo();
            commitInfo.setSha(commit.path("sha").asText().substring(0, 8));
            commitInfo.setMessage(commit.path("commit").path("message").asText());
            commitInfo.setAuthorEmail(commit.path("commit").path("author").path("email").asText("-"));
            commitInfo.setCommittedAt(commit.path("commit").path("author").path("date").asText("-"));
            commitInfos.add(commitInfo);
        }
        return commitInfos;
    }

    private List<GithubFileInfo> readFiles(String repositoryPath, String branch, String directory) {
        String contentsPath = directory.isBlank() ? "/contents" : "/contents/" + directory;
        JsonNode contents = getJson(GITHUB_API + repositoryPath + contentsPath + "?ref=" + encodeQueryValue(branch));
        List<GithubFileInfo> files = new ArrayList<>();
        for (JsonNode content : contents) {
            GithubFileInfo file = new GithubFileInfo();
            file.setName(content.path("name").asText());
            file.setPath(content.path("path").asText());
            file.setType(content.path("type").asText());
            files.add(file);
        }
        return files;
    }

    private void setSelectedFileContent(GithubRepositoryInfo info, String repositoryPath, String requestedFilePath) {
        String filePath = requestedFilePath;
        if (filePath == null) {
            info.setSelectedFileContent("표시할 파일이 없습니다.");
            return;
        }

        try {
            JsonNode file = getJson(GITHUB_API + repositoryPath + "/contents/" + encodePath(filePath)
                    + "?ref=" + encodeQueryValue(info.getSelectedBranch()));
            String encodedContent = file.path("content").asText().replaceAll("\\s", "");
            info.setSelectedFilePath(file.path("path").asText(filePath));
            info.setSelectedFileContent(new String(Base64.getDecoder().decode(encodedContent), StandardCharsets.UTF_8));
        } catch (IllegalStateException ex) {
            // 일부 대용량·바이너리 파일은 Contents API에서 본문을 제공하지 않을 수 있습니다.
            info.setSelectedFilePath(filePath);
            info.setSelectedFileContent("이 파일의 내용은 GitHub API에서 바로 표시할 수 없습니다.");
        }
    }

    private JsonNode getJson(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .header("Accept", "application/vnd.github+json")
                    .header("User-Agent", "weple-repository-viewer")
                    .header("Authorization", "Bearer " + githubToken) // 임시로 깃허브 토큰 제공
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("GitHub 저장소 정보를 조회할 수 없습니다.");
            }
            return objectMapper.readTree(response.body());
        } catch (Exception ex) {
            throw new IllegalStateException("GitHub 저장소 정보를 조회할 수 없습니다.");
        }
    }

    private String toRepositoryPath(String repositoryUrl) {
        String path = repositoryUrl.replaceFirst("^https://github\\.com/", "");
        return path.replaceFirst("\\.git/?$", "").replaceAll("/$", "");
    }

    private String parentDirectory(String directory) {
        int separatorIndex = directory.lastIndexOf('/');
        return separatorIndex < 0 ? "" : directory.substring(0, separatorIndex);
    }

    private String encodePath(String path) {
        String[] pathSegments = path.split("/");
        List<String> encodedSegments = new ArrayList<>();
        for (String pathSegment : pathSegments) {
            encodedSegments.add(encodeQueryValue(pathSegment));
        }
        return String.join("/", encodedSegments);
    }

    private String encodeQueryValue(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
