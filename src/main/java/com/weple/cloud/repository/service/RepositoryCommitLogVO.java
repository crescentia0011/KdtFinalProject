package com.weple.cloud.repository.service;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RepositoryCommitLogVO {

    // commit_logs PK 필요함
    private String commitId;

    // 어떤 저장소의 커밋인지 구분함
    private String repositoryId;

    // GitHub 전체 커밋 해시 저장함
    private String commitHash;

    // 커밋 작성자 이메일 저장함
    private String commitUser;

    // 커밋 메시지 원문 저장함
    private String commitMessage;

    // 화면 표시용 커밋 일시 저장함
    private String committedAt;

    // 처음 수집할 때 연결된 일감 코드 저장함
    private String taskId;
}
