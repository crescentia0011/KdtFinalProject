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
public class ProjectFileVO {
	// 파일 (file)
	private String fileId; // 첨부파일 아이디
	private String taskId; // 일감 아이디
	private Long postId; // 게시글 아이디
	private String logicalName; // 파일명
	private String isDeleted; // 삭제여부
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date createdAt; // 첨부일시
	private Long projectId;
	
	// JOIN 필드 ✅
    private String taskTitle;
    private String projectTitle;
    private long versionNumber;
    private long fileSize;
    private String uploader;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date uploadedAt;
    private String userName;
    private String typeName;
    private String taskType;
    private String hashtag;
    private String versionId;
    
}
