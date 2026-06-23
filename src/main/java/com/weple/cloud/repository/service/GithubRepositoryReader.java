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
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

// GitHub REST API 응답을 저장소 상세와 파일 비교 화면용 데이터로 가공하는 서비스임.
@Service
public class GithubRepositoryReader {

    private static final String GITHUB_API = "https://api.github.com/repos/";
    
    // 토큰은 선택값입니다. 없으면 공개 GitHub API를 비인증으로 호출.
    @Value("${github.api.token:}")
    private String githubToken;
    
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 상세 화면 최초 진입 시 메타데이터, 브랜치, 폴더, 최근 커밋을 한 번에 준비함.
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

    // 선택 파일만 대상으로 최근 두 변경 커밋을 조회해 이전·최신 파일 내용을 만듦.
    public GithubFileDiffInfo readFileDiff(String repositoryUrl, String branch, String filePath) {
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("비교할 파일을 먼저 선택해 주세요.");
        }

        String repositoryPath = toRepositoryPath(repositoryUrl);
        String selectedBranch = branch == null || branch.isBlank() ? "main" : branch;
        JsonNode commits = getJson(GITHUB_API + repositoryPath + "/commits?sha="
                + encodeQueryValue(selectedBranch) + "&path=" + encodeQueryValue(filePath) + "&per_page=2");

        GithubFileDiffInfo diffInfo = new GithubFileDiffInfo();
        diffInfo.setFilePath(filePath);
        if (commits.isEmpty()) {
            diffInfo.setMessage("선택한 파일의 커밋 내역을 찾을 수 없습니다.");
            return diffInfo;
        }

        JsonNode currentCommit = commits.get(0);
        diffInfo.setCurrentCommitSha(currentCommit.path("sha").asText());
        diffInfo.setCurrentCommitMessage(currentCommit.path("commit").path("message").asText());
        diffInfo.setCurrentContent(readFileContentAtCommit(repositoryPath, filePath, diffInfo.getCurrentCommitSha()));

        if (commits.size() < 2) {
            diffInfo.setMessage("이 파일은 비교할 이전 커밋이 없습니다.");
            diffInfo.setPreviousContent("");
            return diffInfo;
        }

        JsonNode previousCommit = commits.get(1);
        diffInfo.setPreviousCommitSha(previousCommit.path("sha").asText());
        diffInfo.setPreviousCommitMessage(previousCommit.path("commit").path("message").asText());
        diffInfo.setPreviousContent(readFileContentAtCommit(repositoryPath, filePath, diffInfo.getPreviousCommitSha()));
        setLineDiff(diffInfo);
        return diffInfo;
    }


    // LCS 기반 줄 비교 수행함. 공통 줄은 유지하고 이전에만 있으면 removed, 최신에만 있으면 added로 분류함.
    private void setLineDiff(GithubFileDiffInfo diffInfo) {
        String[] previousLines = splitLines(diffInfo.getPreviousContent());
        String[] currentLines = splitLines(diffInfo.getCurrentContent());
        // 지나치게 큰 파일은 메모리 사용을 막기 위해 줄 강조 없이 원문만 보여 줍니다.
        if (previousLines.length > 1500 || currentLines.length > 1500) {
            return;
        }

        int[][] lcs = new int[previousLines.length + 1][currentLines.length + 1];
        for (int previousIndex = previousLines.length - 1; previousIndex >= 0; previousIndex--) {
            for (int currentIndex = currentLines.length - 1; currentIndex >= 0; currentIndex--) {
                lcs[previousIndex][currentIndex] = previousLines[previousIndex].equals(currentLines[currentIndex])
                        ? lcs[previousIndex + 1][currentIndex + 1] + 1
                        : Math.max(lcs[previousIndex + 1][currentIndex], lcs[previousIndex][currentIndex + 1]);
            }
        }

        List<GithubDiffLine> previousResult = new ArrayList<>();
        List<GithubDiffLine> currentResult = new ArrayList<>();
        int previousIndex = 0;
        int currentIndex = 0;
        while (previousIndex < previousLines.length && currentIndex < currentLines.length) {
            if (previousLines[previousIndex].equals(currentLines[currentIndex])) {
                previousResult.add(new GithubDiffLine(previousIndex + 1, previousLines[previousIndex], "context"));
                currentResult.add(new GithubDiffLine(currentIndex + 1, currentLines[currentIndex], "context"));
                previousIndex++;
                currentIndex++;
            } else if (lcs[previousIndex + 1][currentIndex] >= lcs[previousIndex][currentIndex + 1]) {
                previousResult.add(new GithubDiffLine(previousIndex + 1, previousLines[previousIndex], "removed"));
                // 좌우 비교 행을 맞추기 위해 최신 파일 쪽에는 빈 행을 함께 추가함.
                currentResult.add(new GithubDiffLine(null, "", "placeholder"));
                previousIndex++;
            } else {
                // 좌우 비교 행을 맞추기 위해 이전 파일 쪽에는 빈 행을 함께 추가함.
                previousResult.add(new GithubDiffLine(null, "", "placeholder"));
                currentResult.add(new GithubDiffLine(currentIndex + 1, currentLines[currentIndex], "added"));
                currentIndex++;
            }
        }
        while (previousIndex < previousLines.length) {
            previousResult.add(new GithubDiffLine(previousIndex + 1, previousLines[previousIndex++], "removed"));
            currentResult.add(new GithubDiffLine(null, "", "placeholder"));
        }
        while (currentIndex < currentLines.length) {
            previousResult.add(new GithubDiffLine(null, "", "placeholder"));
            currentResult.add(new GithubDiffLine(currentIndex + 1, currentLines[currentIndex++], "added"));
        }
        diffInfo.setPreviousLines(previousResult);
        diffInfo.setCurrentLines(currentResult);
    }

    // 빈 파일은 빈 줄 한 개가 아니라 줄이 없는 파일로 처리해야 가짜 삭제 표시가 생기지 않음.
    private String[] splitLines(String content) {
        return content == null || content.isEmpty() ? new String[0] : content.split("\\R", -1);
    }

    // 브랜치 목록 실패 시 상세 화면 전체를 막지 않고 기본 브랜치만 보여 주기 위한 보조 메서드임.
    private List<String> readBranchesOrDefault(String repositoryPath, String defaultBranch) {
        try {
            return readBranches(repositoryPath);
        } catch (IllegalStateException ex) {
            return List.of(defaultBranch);
        }
    }

    // 파일 목록 실패 시 빈 트리를 반환해 다른 상세 정보는 계속 표시되게 함.
    private List<GithubFileInfo> readFilesOrEmpty(String repositoryPath, String branch, String directory) {
        try {
            return readFiles(repositoryPath, branch, directory);
        } catch (IllegalStateException ex) {
            return List.of();
        }
    }

    // 커밋 목록 실패도 상세 화면 전체 실패로 번지지 않게 빈 목록으로 처리함.
    private List<GithubCommitInfo> readCommitsOrEmpty(String repositoryPath, String branch) {
        try {
            return readCommits(repositoryPath, branch);
        } catch (IllegalStateException ex) {
            return List.of();
        }
    }

    // /branches API에서 최대 100개 브랜치 이름만 추려 화면 선택 목록으로 반환함.
    private List<String> readBranches(String repositoryPath) {
        JsonNode branches = getJson(GITHUB_API + repositoryPath + "/branches?per_page=100");
        List<String> branchNames = new ArrayList<>();
        for (JsonNode branch : branches) {
            branchNames.add(branch.path("name").asText());
        }
        return branchNames;
    }

    // /commits API에서 최근 10개만 가져옴. 상세 화면 표에는 최근 내역만 필요함.
    private List<GithubCommitInfo> readCommits(String repositoryPath, String branch) {
        JsonNode commits = getJson(GITHUB_API + repositoryPath + "/commits?sha=" + encodeQueryValue(branch) + "&per_page=10");
        List<GithubCommitInfo> commitInfos = new ArrayList<>();
        for (JsonNode commit : commits) {
            GithubCommitInfo commitInfo = new GithubCommitInfo();
            String fullSha = commit.path("sha").asText();
            commitInfo.setFullSha(fullSha);
            commitInfo.setSha(fullSha.substring(0, Math.min(fullSha.length(), 8)));
            commitInfo.setCommitUrl("https://github.com/" + repositoryPath + "/commit/" + fullSha);
            commitInfo.setMessage(commit.path("commit").path("message").asText());
            commitInfo.setAuthorEmail(commit.path("commit").path("author").path("email").asText("-"));
            commitInfo.setCommittedAt(commit.path("commit").path("author").path("date").asText("-"));
            commitInfos.add(commitInfo);
        }
        return commitInfos;
    }

    // /contents API 사용함. 빈 directory는 루트, 값이 있으면 해당 폴더의 직계 항목을 조회함.
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

    // 선택 파일 본문을 Contents API에서 Base64로 받아 UTF-8 문자열로 복호화함.
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
            // 일부 대용량·바이너리 파일은 Contents API에서 본문을 제공하지 않을 수 있음.
            info.setSelectedFilePath(filePath);
            info.setSelectedFileContent("이 파일의 내용은 GitHub API에서 바로 표시할 수 없습니다.");
        }
    }

    // 특정 SHA를 ref로 넘겨 해당 커밋 시점 파일을 읽음. 404만 빈 값으로 처리해 추가·삭제를 표현함.
    private String readFileContentAtCommit(String repositoryPath, String filePath, String commitSha) {
        try {
            JsonNode file = getJson(GITHUB_API + repositoryPath + "/contents/" + encodePath(filePath)
                    + "?ref=" + encodeQueryValue(commitSha));
            String encodedContent = file.path("content").asText().replaceAll("\\s", "");
            return new String(Base64.getDecoder().decode(encodedContent), StandardCharsets.UTF_8);
        } catch (GithubApiException ex) {
            // 해당 커밋에 파일이 없는 경우만 추가·삭제 파일로 판단함.
            if (ex.getStatusCode() == 404) {
                return "";
            }
            throw ex;
        }
    }

    // 모든 GitHub GET 요청 공통 처리부임. 토큰이 있으면 rate limit 확장용 Authorization 헤더도 붙임.
    private JsonNode getJson(String url) {
        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(URI.create(url))
                    .header("Accept", "application/vnd.github+json")
                    .header("User-Agent", "weple-repository-viewer")
                    .GET();
            // 빈 Bearer 헤더는 GitHub에서 잘못된 인증 요청으로 처리될 수 있습니다.
            if (githubToken != null && !githubToken.isBlank()) {
                requestBuilder.header("Authorization", "Bearer " + githubToken.trim());
            }
            HttpRequest request = requestBuilder.build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new GithubApiException(response.statusCode(), "GitHub 저장소 정보를 조회할 수 없습니다.");
            }
            return objectMapper.readTree(response.body());
        } catch (GithubApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("GitHub 저장소 정보를 조회할 수 없습니다.");
        }
    }

    // HTTP 상태 코드를 보존해 404와 인증·요청 제한 오류를 서로 다르게 처리하기 위한 예외임.
    private static class GithubApiException extends IllegalStateException {

        private final int statusCode;

        private GithubApiException(int statusCode, String message) {
            super(message);
            this.statusCode = statusCode;
        }

        private int getStatusCode() {
            return statusCode;
        }
    }

    // 저장된 GitHub URL을 API가 요구하는 owner/repository 형식으로 정규화함.
    private String toRepositoryPath(String repositoryUrl) {
        String path = repositoryUrl.replaceFirst("^https://github\\.com/", "");
        return path.replaceFirst("\\.git/?$", "").replaceAll("/$", "");
    }

    // 현재 폴더의 상위 폴더 경로 계산함. 루트면 빈 문자열 반환함.
    private String parentDirectory(String directory) {
        int separatorIndex = directory.lastIndexOf('/');
        return separatorIndex < 0 ? "" : directory.substring(0, separatorIndex);
    }

    // 파일 경로는 슬래시 유지하고 각 경로 조각만 인코딩해야 GitHub API가 정상 인식함.
    private String encodePath(String path) {
        String[] pathSegments = path.split("/");
        List<String> encodedSegments = new ArrayList<>();
        for (String pathSegment : pathSegments) {
            encodedSegments.add(encodeQueryValue(pathSegment));
        }
        return String.join("/", encodedSegments);
    }

    // 공백을 + 대신 %20으로 바꿔 query string과 path segment 양쪽에서 안전하게 사용함.
    private String encodeQueryValue(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
