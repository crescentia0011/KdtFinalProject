package com.weple.cloud.repository.service;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GithubDiffLine {

    // 원본 파일에서의 줄 번호이며, 추가 줄은 이전 파일에 줄 번호가 없습니다.
    private final Integer lineNumber;
    private final String content;
    private final String type;
}
