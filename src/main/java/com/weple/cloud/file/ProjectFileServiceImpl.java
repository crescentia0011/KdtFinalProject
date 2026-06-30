package com.weple.cloud.file;

import java.util.List;

import org.springframework.stereotype.Service;

import com.weple.cloud.file.mapper.FileMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProjectFileServiceImpl implements ProjectFileService {

	private final FileMapper fileMapper;
	
	
	// -------------------------------파일관리------------------------------
	// 전체조회
	@Override
	public List<ProjectFileVO> findProjectFileAll(String projectId) {
		return fileMapper.projectFileAll(projectId);
	}

	// 상세조회
	@Override
	public ProjectFileVO findProjectFileInfo(String fileId) {
		return fileMapper.projectFileInfo(fileId);
	}

	// 등록
	@Override
	public String addProjectFile(ProjectFileVO projectFileVO) {
	    long result = fileMapper.insertProjectFile(projectFileVO);
	    return result == 1 ? projectFileVO.getFileId() : "-1";
	}

	// 삭제
	@Override
	public long removeProjectFile(String fileId) {
		long result = fileMapper.deleteProjectFile(fileId);
		return result;
	}
	
	// 프로젝트별 구분
	@Override
	public long removeProjectFileVersionByFileId(String fileId) {
	    return fileMapper.deleteProjectFileVersionByFileId(fileId);
	}
	
	// 다운로드
	@Override
	public ProjectFileVersionsVO findVersionForDownload(String versionId) {
	    return fileMapper.findVersionForDownload(versionId);
	}

	// -------------------------------파일 버전------------------------------
	// 전체조회
	@Override
	public List<ProjectFileVersionsVO> findProjectFileVersionAll(String fileId) {
		return fileMapper.projectFileVersionAll(fileId);
	}

	// 상세조회
	@Override
	public ProjectFileVersionsVO findProjectFileVersionInfo(String versionId) {
		return fileMapper.projectFileVersionInfo(versionId);
	}

	// 등록
	@Override
	public String addProjectFileVersion(ProjectFileVersionsVO projectFileVersionsVO) {
	    long result = fileMapper.insertProjectFileVersion(projectFileVersionsVO);
	    return result == 1 ? projectFileVersionsVO.getVersionId() : "-1";
	}

	// 삭제
	@Override
	public long removeProjectFileVersion(String versionId) {
		long result = fileMapper.deleteProjectFileVersion(versionId);
		return result;
	}
	
	// -------------------------------파일 히스토리------------------------------
   	// 다운로드 이력
	@Override
	public void recordDownloadHistory(String versionId, String downloader) {
	    DownloadHistoryVO vo = new DownloadHistoryVO();
	    vo.setVersionId(versionId);
	    vo.setDownloader(downloader);
	    fileMapper.insertDownloadHistory(vo);
	}

	@Override
	public List<DownloadHistoryVO> findDownloadHistory(String projectId) {
	    return fileMapper.findDownloadHistory(projectId);
	}
}