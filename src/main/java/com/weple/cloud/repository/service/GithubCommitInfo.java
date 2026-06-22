package com.weple.cloud.repository.service;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GithubCommitInfo {

    private String sha;
    private String message;
    private String authorEmail;
    private String committedAt;
}
