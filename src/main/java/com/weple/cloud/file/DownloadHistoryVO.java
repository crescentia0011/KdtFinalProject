package com.weple.cloud.file;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class DownloadHistoryVO {
    private String historyId;
    private String versionId;
    private String downloader;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private Date downloadedAt;

    // 조회용 JOIN 필드
    private String downloaderName;   // 다운로더 이름
    private String logicalName;      // 파일명
    private String taskId;
    private String taskTitle;
    private Long fileSize;
    private Long versionNumber;
}