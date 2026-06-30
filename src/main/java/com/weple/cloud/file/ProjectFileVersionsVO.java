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
public class ProjectFileVersionsVO {
	// 파일 버전 (file_versions)
	private String versionId;                  //버전 아이디
	private String fileId;                     //첨부파일 아이디
	private long versionNumber;              //버전 번호
	private String savedName;                //저장 경로
	private String filePath;                 //파일 크기
	private long fileSize;                   //파일 등록자
	private String uploader;                  //등록일자
	@DateTimeFormat(pattern = "yyyy-MM-dd") 
	private Date uploadedAt;                 //파일 서버 저장명
	private String hashtag;                  //해시태그
	
	private String taskType;
	private String logicalName;
}
