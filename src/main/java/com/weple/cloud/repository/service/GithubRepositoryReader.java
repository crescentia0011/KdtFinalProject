package com.weple.cloud.repository.service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

// GitHub REST API 응답을 저장소 상세, 커밋 상세, 파일 비교 화면에서 쓰기 좋은 데이터로 가공하는 서비스입니다.
@Service
public class GithubRepositoryReader {

    private static final String GITHUB_API = "https://api.github.com/repos/";
    
    // 토큰은 선택 값입니다. 없으면 공개 GitHub API를 비인증으로 호출합니다.
    @Value("${github.api.token:}")
    private String githubToken;
    
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final int COMMIT_PAGE_SIZE = 10;
    private static final int COMMIT_PAGE_BLOCK_SIZE = 10;
    private static final Pattern TASK_CODE_PATTERN = Pattern.compile("TSK-\\d{6}_\\d+");
    private static final ZoneId DISPLAY_TIME_ZONE = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter COMMIT_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(DISPLAY_TIME_ZONE);

    // 저장소 상세 화면 최초 진입에 필요한 메타데이터, 브랜치, 파일 목록, 커밋 목록을 한 번에 준비합니다.
    public GithubRepositoryInfo readRepository(String repositoryUrl, String requestedBranch,
                                               String requestedDirectory, String requestedFilePath,
                                               int requestedCommitPage,
                                               LocalDate commitStartDate, LocalDate commitEndDate) {
        return readRepository(repositoryUrl, requestedBranch, requestedDirectory, requestedFilePath,
                requestedCommitPage, commitStartDate, commitEndDate, null);
    }

    // 저장소 설정값까지 받아 커밋 메시지의 일감 연결 규칙을 같이 적용함
    public GithubRepositoryInfo readRepository(String repositoryUrl, String requestedBranch,
                                               String requestedDirectory, String requestedFilePath,
                                               int requestedCommitPage,
                                               LocalDate commitStartDate, LocalDate commitEndDate,
                                               RepositoryManageSettingVO repositorySetting) {
        String repositoryPath = toRepositoryPath(repositoryUrl);
        JsonNode repository = getJson(GITHUB_API + repositoryPath);

        GithubRepositoryInfo info = new GithubRepositoryInfo();
        info.setDescription(repository.path("description").asText("저장소 설명이 없습니다."));
        info.setDefaultBranch(repository.path("default_branch").asText("main"));
        info.setSelectedBranch(requestedBranch == null || requestedBranch.isBlank()
                ? info.getDefaultBranch() : requestedBranch);
        info.setCurrentDirectory(requestedDirectory == null ? "" : requestedDirectory);
        info.setParentDirectory(parentDirectory(info.getCurrentDirectory()));
        info.setCommitPage(Math.max(requestedCommitPage, 1));

        // 서로 의존하지 않는 GitHub 조회는 병렬로 실행해 상세 화면 대기 시간을 줄입니다.
        CompletableFuture<List<String>> branchesFuture = CompletableFuture.supplyAsync(
                () -> readBranchesOrDefault(repositoryPath, info.getDefaultBranch()));
        CompletableFuture<List<GithubFileInfo>> filesFuture = CompletableFuture.supplyAsync(
                () -> readFilesOrEmpty(repositoryPath, info.getSelectedBranch(), info.getCurrentDirectory()));
        CompletableFuture<GithubCommitPage> commitsFuture = CompletableFuture.supplyAsync(
                () -> readCommitsOrEmpty(repositoryPath, info.getSelectedBranch(), info.getCommitPage(),
                        commitStartDate, commitEndDate, repositorySetting));

        info.setBranches(branchesFuture.join());
        info.setFiles(filesFuture.join());
        GithubCommitPage commitPage = commitsFuture.join();
        info.setCommits(commitPage.getCommits());
        info.setTotalCommitPages(commitPage.getTotalPages());
        info.setHasNextCommitPage(commitPage.isHasNextPage());
        info.setStartCommitPage(((info.getCommitPage() - 1) / COMMIT_PAGE_BLOCK_SIZE) * COMMIT_PAGE_BLOCK_SIZE + 1);
        info.setEndCommitPage(Math.min(info.getStartCommitPage() + COMMIT_PAGE_BLOCK_SIZE - 1,
                info.getTotalCommitPages()));

        // 파일 본문은 사용자가 파일을 선택했을 때만 조회해 초기 진입을 가볍게 유지합니다.
        if (requestedFilePath == null || requestedFilePath.isBlank()) {
            info.setSelectedFileContent("왼쪽 목록에서 파일을 선택하세요.");
        } else {
            setSelectedFileContent(info, repositoryPath, requestedFilePath);
        }
        return info;
    }

    // 선택 파일을 대상으로 최근 두 변경 커밋을 조회해 이전/최신 파일 내용을 만듭니다.
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

    // 선택한 커밋 1건의 기본 정보와 변경 파일 목록을 GitHub commit detail API에서 조회합니다.
    public GithubCommitDetailInfo readCommitDetail(String repositoryUrl, String commitSha) {
        if (commitSha == null || commitSha.isBlank()) {
            throw new IllegalArgumentException("조회할 커밋 해시코드가 없습니다.");
        }

        String repositoryPath = toRepositoryPath(repositoryUrl);
        JsonNode commit = getJson(GITHUB_API + repositoryPath + "/commits/" + encodeQueryValue(commitSha));

        GithubCommitDetailInfo detailInfo = new GithubCommitDetailInfo();
        String fullSha = commit.path("sha").asText(commitSha);
        detailInfo.setSha(fullSha);
        detailInfo.setShortSha(fullSha.substring(0, Math.min(fullSha.length(), 8)));
        detailInfo.setMessage(commit.path("commit").path("message").asText("커밋 메시지가 없습니다."));
        detailInfo.setAuthorEmail(commit.path("commit").path("author").path("email").asText("-"));
        detailInfo.setCommittedAt(formatCommitDate(commit.path("commit").path("author").path("date").asText("-")));
        detailInfo.setCommitUrl(commit.path("html_url").asText("https://github.com/" + repositoryPath + "/commit/" + fullSha));
        detailInfo.setTotalAdditions(commit.path("stats").path("additions").asInt());
        detailInfo.setTotalDeletions(commit.path("stats").path("deletions").asInt());
        detailInfo.setTotalChanges(commit.path("stats").path("total").asInt());
        List<GithubCommitFileChangeInfo> files = readCommitFiles(commit.path("files"));
        detailInfo.setFiles(files);
        detailInfo.setFileTree(toCommitFileTree(files));
        return detailInfo;
    }


    // LCS 기반 줄 비교를 수행해 공통 줄은 context, 이전에만 있으면 removed, 최신에만 있으면 added로 분류합니다.
    private void setLineDiff(GithubFileDiffInfo diffInfo) {
        String[] previousLines = splitLines(diffInfo.getPreviousContent());
        String[] currentLines = splitLines(diffInfo.getCurrentContent());
        // 지나치게 큰 파일은 메모리 사용을 막기 위해 강조 diff 없이 원문만 보여 줍니다.
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
                // 좌우 비교 행을 맞추기 위해 최신 파일 쪽에 빈 행을 함께 추가합니다.
                currentResult.add(new GithubDiffLine(null, "", "placeholder"));
                previousIndex++;
            } else {
                // 좌우 비교 행을 맞추기 위해 이전 파일 쪽에 빈 행을 함께 추가합니다.
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

    // 빈 파일은 빈 줄 1개가 아니라 줄이 없는 파일로 처리합니다.
    private String[] splitLines(String content) {
        return content == null || content.isEmpty() ? new String[0] : content.split("\\R", -1);
    }

    // GitHub commit detail 응답의 files 배열을 화면 출력용 변경 파일 정보로 변환합니다.
    private List<GithubCommitFileChangeInfo> readCommitFiles(JsonNode files) {
        List<GithubCommitFileChangeInfo> fileChanges = new ArrayList<>();
        if (files == null || !files.isArray()) {
            return fileChanges;
        }

        int fileIndex = 0;
        for (JsonNode file : files) {
            GithubCommitFileChangeInfo fileChange = new GithubCommitFileChangeInfo();
            fileChange.setFilename(file.path("filename").asText());
            fileChange.setPreviousFilename(file.path("previous_filename").asText(""));
            fileChange.setAnchorId("commit-file-" + fileIndex++);
            fileChange.setStatus(file.path("status").asText());
            fileChange.setStatusLabel(toFileStatusLabel(fileChange.getStatus()));
            fileChange.setAdditions(file.path("additions").asInt());
            fileChange.setDeletions(file.path("deletions").asInt());
            fileChange.setChanges(file.path("changes").asInt());
            fileChange.setPatch(file.path("patch").asText(""));
            fileChange.setPatchLines(toPatchLines(fileChange.getPatch()));
            fileChanges.add(fileChange);
        }
        return fileChanges;
    }

    // 파일 경로를 폴더/파일 행으로 펼쳐 왼쪽 파일트리에서 사용할 데이터를 만듭니다.
    private List<GithubCommitFileTreeItem> toCommitFileTree(List<GithubCommitFileChangeInfo> files) {
        List<GithubCommitFileTreeItem> treeItems = new ArrayList<>();
        Set<String> addedDirectories = new HashSet<>();
        List<GithubCommitFileChangeInfo> sortedFiles = new ArrayList<>(files);
        sortedFiles.sort(Comparator.comparing(GithubCommitFileChangeInfo::getFilename));

        for (GithubCommitFileChangeInfo file : sortedFiles) {
            String[] pathSegments = file.getFilename().split("/");
            StringBuilder currentPath = new StringBuilder();

            for (int index = 0; index < pathSegments.length - 1; index++) {
                if (currentPath.length() > 0) {
                    currentPath.append("/");
                }
                currentPath.append(pathSegments[index]);

                String directoryPath = currentPath.toString();
                if (addedDirectories.add(directoryPath)) {
                    treeItems.add(createDirectoryTreeItem(pathSegments[index], directoryPath, index));
                }
            }

            treeItems.add(createFileTreeItem(pathSegments[pathSegments.length - 1], file.getFilename(),
                    Math.max(pathSegments.length - 1, 0), file));
        }
        return treeItems;
    }

    // 폴더 행은 실제 파일 변경 정보가 없고, 하위 파일을 묶는 용도로만 사용합니다.
    private GithubCommitFileTreeItem createDirectoryTreeItem(String name, String path, int level) {
        GithubCommitFileTreeItem treeItem = new GithubCommitFileTreeItem();
        treeItem.setName(name);
        treeItem.setPath(path);
        treeItem.setType("directory");
        treeItem.setLevel(level);
        treeItem.setIndent(level * 16);
        return treeItem;
    }

    // 파일 행은 오른쪽 diff 영역으로 이동할 수 있도록 실제 변경 파일 정보를 연결합니다.
    private GithubCommitFileTreeItem createFileTreeItem(String name, String path, int level,
                                                        GithubCommitFileChangeInfo file) {
        GithubCommitFileTreeItem treeItem = new GithubCommitFileTreeItem();
        treeItem.setName(name);
        treeItem.setPath(path);
        treeItem.setType("file");
        treeItem.setLevel(level);
        treeItem.setIndent(level * 16);
        treeItem.setFile(file);
        return treeItem;
    }

    // GitHub patch 문자열을 줄 단위로 나누고 추가/삭제/헤더/문맥 줄로 구분합니다.
    private List<GithubPatchLine> toPatchLines(String patch) {
        List<GithubPatchLine> patchLines = new ArrayList<>();
        if (patch == null || patch.isBlank()) {
            return patchLines;
        }

        for (String line : patch.split("\\R", -1)) {
            patchLines.add(new GithubPatchLine(toPatchLineType(line), line));
        }
        return patchLines;
    }

    // patch 줄의 앞 글자를 기준으로 화면에서 사용할 diff 표시 타입을 결정합니다.
    private String toPatchLineType(String line) {
        if (line.startsWith("@@")) {
            return "header";
        }
        if (line.startsWith("+") && !line.startsWith("+++")) {
            return "added";
        }
        if (line.startsWith("-") && !line.startsWith("---")) {
            return "removed";
        }
        return "context";
    }

    // GitHub API의 영문 변경 상태를 화면에서 쓰는 한국어 상태명으로 바꿉니다.
    private String toFileStatusLabel(String status) {
        if (status == null || status.isBlank()) {
            return "\uBCC0\uACBD";
        }

        return switch (status) {
            case "added" -> "\uCD94\uAC00";
            case "removed" -> "\uC0AD\uC81C";
            case "renamed" -> "\uC774\uB984\uBCC0\uACBD";
            case "modified" -> "\uC218\uC815";
            default -> status;
        };
    }

    // 브랜치 목록 조회에 실패해도 상세 화면 전체가 깨지지 않도록 기본 브랜치만 반환합니다.
    private List<String> readBranchesOrDefault(String repositoryPath, String defaultBranch) {
        try {
            return readBranches(repositoryPath);
        } catch (IllegalStateException ex) {
            return List.of(defaultBranch);
        }
    }

    // 파일 목록 조회 실패 시 빈 목록을 반환해 나머지 상세 정보는 계속 표시합니다.
    private List<GithubFileInfo> readFilesOrEmpty(String repositoryPath, String branch, String directory) {
        try {
            return readFiles(repositoryPath, branch, directory);
        } catch (IllegalStateException ex) {
            return List.of();
        }
    }

    // 커밋 목록 조회 실패가 상세 화면 전체 실패로 번지지 않게 빈 페이지 정보로 처리합니다.
    private GithubCommitPage readCommitsOrEmpty(String repositoryPath, String branch, int page,
                                                LocalDate commitStartDate, LocalDate commitEndDate,
                                                RepositoryManageSettingVO repositorySetting) {
        try {
            return readCommits(repositoryPath, branch, page, commitStartDate, commitEndDate, repositorySetting);
        } catch (IllegalStateException ex) {
            return new GithubCommitPage(List.of(), page, false);
        }
    }

    // /branches API에서 최대 100개 브랜치 이름만 추려 화면 선택 목록으로 반환합니다.
    private List<String> readBranches(String repositoryPath) {
        JsonNode branches = getJson(GITHUB_API + repositoryPath + "/branches?per_page=100");
        List<String> branchNames = new ArrayList<>();
        for (JsonNode branch : branches) {
            branchNames.add(branch.path("name").asText());
        }
        return branchNames;
    }

    // /commits API의 Link 헤더에서 다음/마지막 페이지를 확인해 10개 단위 페이징에 사용합니다.
    private GithubCommitPage readCommits(String repositoryPath, String branch, int page,
                                         LocalDate commitStartDate, LocalDate commitEndDate,
                                         RepositoryManageSettingVO repositorySetting) {
        String commitUrl = GITHUB_API + repositoryPath + "/commits?sha="
                + encodeQueryValue(branch) + "&per_page=" + COMMIT_PAGE_SIZE + "&page=" + page
                + toCommitDateQuery(commitStartDate, commitEndDate);
        GithubApiResponse response = getJsonResponse(commitUrl);
        JsonNode commits = response.getBody();
        List<GithubCommitInfo> commitInfos = new ArrayList<>();
        for (JsonNode commit : commits) {
            GithubCommitInfo commitInfo = new GithubCommitInfo();
            String fullSha = commit.path("sha").asText();
            commitInfo.setFullSha(fullSha);
            commitInfo.setSha(fullSha.substring(0, Math.min(fullSha.length(), 8)));
            commitInfo.setCommitUrl("https://github.com/" + repositoryPath + "/commit/" + fullSha);
            commitInfo.setMessage(commit.path("commit").path("message").asText());
            commitInfo.setTaskId(extractTaskId(commitInfo.getMessage(), repositorySetting));
            commitInfo.setAuthorEmail(commit.path("commit").path("author").path("email").asText("-"));
            commitInfo.setCommittedAt(formatCommitDate(commit.path("commit").path("author").path("date").asText("-")));
            commitInfos.add(commitInfo);
        }
        String linkHeader = response.getLinkHeader();
        boolean hasNextPage = linkHeader.contains("rel=\"next\"");
        int totalPages = extractLastPage(linkHeader, page, hasNextPage);
        return new GithubCommitPage(commitInfos, totalPages, hasNextPage);
    }

    // 참조 키워드가 붙은 일감 코드만 커밋 연결 대상으로 인식함
    private String extractTaskId(String commitMessage, RepositoryManageSettingVO repositorySetting) {
        if (commitMessage == null || commitMessage.isBlank()) {
            return null;
        }
        if (repositorySetting == null || !"Y".equals(repositorySetting.getCommitTextYn())) {
            return null;
        }

        List<String> keywords = parseReferenceKeywords(repositorySetting.getTaskKeyword());
        if (keywords.isEmpty()) {
            return null;
        }

        // refs TSK-260624_5 같은 형태만 실제 일감 코드로 추출함
        String keywordPattern = keywords.stream()
                .map(Pattern::quote)
                .collect(Collectors.joining("|"));
        Pattern taskReferencePattern = Pattern.compile(
                "(?i)(?:^|\\s)(?:" + keywordPattern + ")\\s+(" + TASK_CODE_PATTERN.pattern() + ")(?=\\s|$)");
        Matcher matcher = taskReferencePattern.matcher(commitMessage);
        return matcher.find() ? matcher.group(1) : null;
    }

    // 쉼표로 저장된 참조 키워드를 검색 가능한 목록으로 정리함
    private List<String> parseReferenceKeywords(String keywords) {
        if (keywords == null || keywords.isBlank()) {
            return List.of();
        }
        return java.util.Arrays.stream(keywords.split(","))
                .map(String::trim)
                .filter(keyword -> !keyword.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }

    // 화면에서 선택한 날짜를 GitHub API가 받는 UTC ISO 시간 조건으로 변환합니다.
    private String toCommitDateQuery(LocalDate commitStartDate, LocalDate commitEndDate) {
        StringBuilder query = new StringBuilder();
        if (commitStartDate != null) {
            String since = commitStartDate.atStartOfDay(DISPLAY_TIME_ZONE).toInstant().toString();
            query.append("&since=").append(encodeQueryValue(since));
        }
        if (commitEndDate != null) {
            String until = commitEndDate.atTime(LocalTime.of(23, 59, 59)).atZone(DISPLAY_TIME_ZONE)
                    .toInstant().toString();
            query.append("&until=").append(encodeQueryValue(until));
        }
        return query.toString();
    }

    // GitHub API의 UTC ISO 시간을 화면에서 읽기 쉬운 한국 시간으로 변환합니다.
    private String formatCommitDate(String githubDate) {
        if (githubDate == null || githubDate.isBlank() || "-".equals(githubDate)) {
            return "-";
        }

        try {
            return COMMIT_DATE_FORMATTER.format(Instant.parse(githubDate));
        } catch (DateTimeParseException ex) {
            return githubDate;
        }
    }

    // /contents API를 사용해 현재 폴더의 직계 파일/폴더만 조회합니다.
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

    // 선택 파일 본문을 Contents API에서 Base64로 받아 UTF-8 문자열로 복호화합니다.
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
            // 일부 대용량/바이너리 파일은 Contents API에서 본문을 제공하지 않을 수 있습니다.
            info.setSelectedFilePath(filePath);
            info.setSelectedFileContent("이 파일의 내용은 GitHub API에서 바로 표시할 수 없습니다.");
        }
    }

    // 특정 SHA를 ref로 넘겨 해당 커밋 시점의 파일 내용을 읽습니다. 404는 추가/삭제 파일 표현을 위해 빈 값으로 처리합니다.
    private String readFileContentAtCommit(String repositoryPath, String filePath, String commitSha) {
        try {
            JsonNode file = getJson(GITHUB_API + repositoryPath + "/contents/" + encodePath(filePath)
                    + "?ref=" + encodeQueryValue(commitSha));
            String encodedContent = file.path("content").asText().replaceAll("\\s", "");
            return new String(Base64.getDecoder().decode(encodedContent), StandardCharsets.UTF_8);
        } catch (GithubApiException ex) {
            // 해당 커밋에 파일이 없는 경우만 추가/삭제 파일로 판단합니다.
            if (ex.getStatusCode() == 404) {
                return "";
            }
            throw ex;
        }
    }

    // JSON 본문만 필요한 GitHub API 요청에서 공통 응답 처리를 재사용합니다.
    private JsonNode getJson(String url) {
        return getJsonResponse(url).getBody();
    }

    // GitHub Link 헤더까지 필요한 요청을 위해 본문과 헤더를 함께 반환합니다.
    private GithubApiResponse getJsonResponse(String url) {
        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(URI.create(url))
                    .header("Accept", "application/vnd.github+json")
                    .header("User-Agent", "weple-repository-viewer")
                    .GET();
            // 빈 Bearer 헤더는 GitHub에서 잘못된 인증 요청으로 처리될 수 있어 토큰이 있을 때만 추가합니다.
            if (githubToken != null && !githubToken.isBlank()) {
                requestBuilder.header("Authorization", "Bearer " + githubToken.trim());
            }
            HttpRequest request = requestBuilder.build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new GithubApiException(response.statusCode(), "GitHub 저장소 정보를 조회할 수 없습니다.");
            }
            return new GithubApiResponse(
                    objectMapper.readTree(response.body()),
                    response.headers().firstValue("Link").orElse(""));
        } catch (GithubApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("GitHub 저장소 정보를 조회할 수 없습니다.");
        }
    }

    // Link 헤더의 rel="last" URL에서 마지막 페이지 번호를 읽습니다.
    private int extractLastPage(String linkHeader, int currentPage, boolean hasNextPage) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("[?&]page=(\\d+)[^>]*>;\\s*rel=\\\"last\\\"")
                .matcher(linkHeader);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        // 마지막 페이지 정보가 생략된 경우에도 다음 이동이 가능하도록 현재 페이지를 기준으로 계산합니다.
        return hasNextPage ? currentPage + 1 : currentPage;
    }

    // HTTP 상태 코드를 보존해 404와 인증/요청 제한 오류를 구분하기 위한 예외입니다.
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

    // GitHub 응답 본문과 Link 헤더를 함께 보관하는 내부 전달 객체입니다.
    private static class GithubApiResponse {

        private final JsonNode body;
        private final String linkHeader;

        private GithubApiResponse(JsonNode body, String linkHeader) {
            this.body = body;
            this.linkHeader = linkHeader;
        }

        private JsonNode getBody() {
            return body;
        }

        private String getLinkHeader() {
            return linkHeader;
        }
    }

    // 커밋 목록과 페이지 이동에 필요한 정보만 묶어 상세 화면에 전달하는 내부 객체입니다.
    private static class GithubCommitPage {

        private final List<GithubCommitInfo> commits;
        private final int totalPages;
        private final boolean hasNextPage;

        private GithubCommitPage(List<GithubCommitInfo> commits, int totalPages, boolean hasNextPage) {
            this.commits = commits;
            this.totalPages = totalPages;
            this.hasNextPage = hasNextPage;
        }

        private List<GithubCommitInfo> getCommits() {
            return commits;
        }

        private int getTotalPages() {
            return totalPages;
        }

        private boolean isHasNextPage() {
            return hasNextPage;
        }
    }

    // 저장된 GitHub URL을 API가 요구하는 owner/repository 형식으로 정규화
    private String toRepositoryPath(String repositoryUrl) {
        String path = repositoryUrl.replaceFirst("^https://github\\.com/", "");
        return path.replaceFirst("\\.git/?$", "").replaceAll("/$", "");
    }

    // 현재 폴더의 상위 폴더 경로를 계산합니다. 루트면 빈 문자열을 반환
    private String parentDirectory(String directory) {
        int separatorIndex = directory.lastIndexOf('/');
        return separatorIndex < 0 ? "" : directory.substring(0, separatorIndex);
    }

    // 파일 경로의 슬래시는 유지하고 각 경로 조각만 인코딩해 GitHub API가 인식할 수 있게 함
    private String encodePath(String path) {
        String[] pathSegments = path.split("/");
        List<String> encodedSegments = new ArrayList<>();
        for (String pathSegment : pathSegments) {
            encodedSegments.add(encodeQueryValue(pathSegment));
        }
        return String.join("/", encodedSegments);
    }

    // 공백을 + 대신 %20으로 바꿔 query string과 path segment에서 안전하게 사용
    private String encodeQueryValue(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
