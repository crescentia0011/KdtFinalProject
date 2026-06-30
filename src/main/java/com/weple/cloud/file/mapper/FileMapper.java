package com.weple.cloud.file.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.weple.cloud.file.FileDownloadDTO;
import com.weple.cloud.file.FileInfoVO;
import com.weple.cloud.file.FileVO;
import com.weple.cloud.file.ProjectFileVO;
import com.weple.cloud.file.DownloadHistoryVO;
import com.weple.cloud.file.ProjectFileVersionsVO;


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
    
    // -------------------------------파일관리------------------------------
 	// 전체조회
    public List<ProjectFileVO> projectFileAll(@Param("projectId") String projectId);
    
    // 상세조회
    public ProjectFileVO projectFileInfo(String fileId);
    
    // 등록
    public long insertProjectFile(ProjectFileVO projectFileVO);
    
    // 삭제
    public long deleteProjectFile(String fileId);
    
    // 프로젝트별 구분
    public long deleteProjectFileVersionByFileId(String fileId);
    
    // 다운로드
    public ProjectFileVersionsVO findVersionForDownload(String versionId);
    
    // -------------------------------파일 버전------------------------------
  	// 전체조회
    List<ProjectFileVersionsVO> projectFileVersionAll(String fileId);
    
    // 상세조회
    public ProjectFileVersionsVO projectFileVersionInfo(String versionId);
    
    // 등록
    public long insertProjectFileVersion(ProjectFileVersionsVO projectFileVersionsVO);
    
    // 삭제
    public long deleteProjectFileVersion(String versionId);
    
    // -------------------------------파일 히스토리------------------------------
   	// 다운로드 이력
    long insertDownloadHistory(DownloadHistoryVO downloadHistoryVO);
    List<DownloadHistoryVO> findDownloadHistory(String projectId);
}