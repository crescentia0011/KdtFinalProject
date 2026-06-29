package com.weple.cloud.file;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class FileDownloadDTO {
	private Long fileId;         
    private Long versionId;       
    private String logicalName;    
    private String savedName;   
    private String filePath;      
    private Long fileSize;        
    private Integer versionNumber;
    private LocalDateTime uploadedAt;

}
