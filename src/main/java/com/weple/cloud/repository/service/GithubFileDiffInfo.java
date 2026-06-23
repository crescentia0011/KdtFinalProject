package com.weple.cloud.repository.service;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GithubFileDiffInfo {

    // 사용자가 선택한 GitHub 파일 경로입니다.
    private String filePath;

    // 파일의 최신 변경 커밋과 바로 이전 변경 커밋입니다.
    private String currentCommitSha;
    private String previousCommitSha;
    private String currentCommitMessage;
    private String previousCommitMessage;

    // 두 커밋 시점의 파일 내용을 좌우 비교 화면에 표시합니다.
    private String currentContent;
    private String previousContent;

    // 줄 단위 비교 결과입니다. context, added, removed 세 유형을 사용합니다.
    private List<GithubDiffLine> previousLines;
    private List<GithubDiffLine> currentLines;

    // 비교할 커밋이 부족하거나 내용을 제공할 수 없을 때 안내합니다.
    private String message;
}
