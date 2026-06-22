package com.weple.cloud.repository.service;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GithubFileInfo {

    // GitHub Contents API가 반환한 파일 또는 폴더 이름과 전체 경로
    private String name;
    private String path;
    private String type;
}
