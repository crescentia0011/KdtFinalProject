package com.weple.cloud.file.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.weple.cloud.file.FileInfoVO;
import com.weple.cloud.file.FileVO;
import com.weple.cloud.file.FileDownloadDTO;


// fileinfoVO , fileVO 재사용 가능 파일 테이블 2개의 VO

public interface FileMapper {
    // 동일한 일감 내 같은 이름의 파일이 존재하는지 조회
	// (@Param)으로 각자 분별할 컬럼값 보내서 쿼리로 넘김
    Long findFileId(@Param("taskId") String taskId, @Param("fileName") String fileName);
    
    // 새 파일 등록
    int insertFile(FileVO fileVO);
    
    // 파일 버전(상세) 정보 등록
    int insertFileInfo(FileInfoVO fileInfoVO);
    
    List<FileInfoVO> findFileInfoByFileId(Long fileId);
    void updateFileDeletedStatus(Long fileId);
    void clearFileVersionInfo(Long fileId);
    void restoreFile(Long fileId);
    
    FileDownloadDTO selectFileForDownload(Long versionId);
}
