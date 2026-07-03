package com.weple.cloud.task.service.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.weple.cloud.file.FileDownloadDTO;
import com.weple.cloud.file.FileInfoVO;
import com.weple.cloud.file.FileVO;
import com.weple.cloud.file.S3Service;
import com.weple.cloud.file.mapper.FileMapper;
import com.weple.cloud.milestone.mapper.MilestoneMapper;
import com.weple.cloud.notification.service.AlarmType;
import com.weple.cloud.notification.service.NotificationService;
import com.weple.cloud.task.mapper.TaskMapper;
import com.weple.cloud.task.service.TaskCommentVO;
import com.weple.cloud.task.service.TaskHistoryDTO;
import com.weple.cloud.task.service.TaskHistoryDetailDTO;
import com.weple.cloud.task.service.TaskMemberVO;
import com.weple.cloud.task.service.TaskMilestoneVO;
import com.weple.cloud.task.service.TaskParentVO;
import com.weple.cloud.task.service.TaskPermissionVO;
import com.weple.cloud.task.service.TaskPriorityVO;
import com.weple.cloud.task.service.TaskProjectSelectVO;
import com.weple.cloud.task.service.TaskService;
import com.weple.cloud.task.service.TaskSpentTimeVO;
import com.weple.cloud.task.service.TaskStatusVO;
import com.weple.cloud.task.service.TaskTestCaseDTO;
import com.weple.cloud.task.service.TaskTypeListVO;
import com.weple.cloud.task.service.TaskUpdateHistoryVO;
import com.weple.cloud.task.service.TaskVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {
	private final FileMapper fileMapper;
	private final TaskMapper taskMapper;
	private final MilestoneMapper milestoneMapper;
	private final NotificationService notificationService;
	private final S3Service s3Service;

	// 내부 일감 전체 조회
	@Override
	public List<TaskVO> findAll(Long pId) {
		return taskMapper.selectAll(pId);
	}

	// 일감유형 목록 조회
	@Override
	public List<TaskTypeListVO> findType(Long cId) {
		return taskMapper.taskTypes(cId);
	}

	// 일감 상태 목록 조회
	@Override
	public List<TaskStatusVO> findStatus() {
		return taskMapper.taskStatuses();
	}

	// 프로젝트 참여자 목록 조회 담당자 지정
	@Override
	public List<TaskMemberVO> findMember(Long pId) {
		return taskMapper.taskMembers(pId);
	}

	// 우선순위 목록 조회
	@Override
	public List<TaskPriorityVO> findPriority(Long cId) {
		return taskMapper.taskPriorities(cId);
	}

	// 상위 일감 목록 조회
	@Override
	public List<TaskParentVO> findParent(Long pId) {
		return taskMapper.taskParents(pId);
	}

	// 마일스톤 목록 조회
	@Override
	public List<TaskMilestoneVO> findMilestone(Long pId) {
		return taskMapper.taskMilestones(pId);
	}

	// 해당 프로젝트 기간
	@Override
	public TaskProjectSelectVO findprojectPeriod(Long pId) {

		return taskMapper.projectPeriod(pId);
	}

	// 등록
	@Override
	@Transactional
	public int insertTask(TaskVO taskVO, List<MultipartFile> files) throws Exception {

		// 일감 DB 등록
		int result = taskMapper.insertTask(taskVO);
		String currentTaskId = taskVO.getTaskId();

		// 알림-은지
		if (result > 0 && taskVO.getTaskManager() != null && !taskVO.getTaskManager().isBlank()) {
			notificationService.create(taskVO.getTaskManager(), AlarmType.TAG_TASK_ASSIGN,
					"새로운 일감 [" + taskVO.getTaskTitle() + "]이 본인에게 배정되었습니다.", AlarmType.TARGET_TASK, taskVO.getTaskId());
		}

		// 파일 체크
		if (files == null || files.isEmpty()) {
			return result;
		}

		for (MultipartFile file : files) {
			if (file.isEmpty())
				continue;

			String originalFileName = file.getOriginalFilename();
			String savedName = UUID.randomUUID().toString() + "_" + originalFileName;

			s3Service.uploadFile(file, savedName); // 로컬 저장 대신 S3로 업로드
			long fileSize = file.getSize();

			Long fileId = fileMapper.findFileId(currentTaskId, originalFileName);

			if (fileId == null) {
				FileVO fileVO = new FileVO();
				fileVO.setTaskId(currentTaskId);
				fileVO.setFileName(originalFileName);
				fileMapper.insertFile(fileVO);
				fileId = fileVO.getFileId();
			}

			FileInfoVO fileInfoVO = new FileInfoVO();
			fileInfoVO.setFileId(fileId);
			fileInfoVO.setFilePath(null); // 더 이상 로컬 경로 안 씀
			fileInfoVO.setFileSize(fileSize);
			fileInfoVO.setUploader(taskVO.getUserCode());
			fileInfoVO.setSavedName(savedName); // S3 키

			fileMapper.insertFileInfo(fileInfoVO);
		}

		// insert 성공하고, milestoneId 값이 null 이 아닐경우 -> 마일스톤 상태값 변경하기 (일감 진척도에 따른 마일스톤
		// 상태[진행 중/완료] 체크)
		if (result > 0 && taskVO.getMilestoneId() != null) {
			syncMilestoneStatus(taskVO.getMilestoneId());
		}

		return result;
	}

	// 상세조회
	@Override
	public TaskVO findTaskDetail(String tId) {
		return taskMapper.taskDetail(tId);
	}

	// 상세 조회 하위 일감
	@Override
	public List<TaskVO> findChildTask(String tId) {
		return taskMapper.childTask(tId);
	}

	// 전체 일감 조회
	@Override
	public List<TaskVO> findAllList(Map<String, Object> allParams) {

		return taskMapper.selectAllList(allParams);
	}

	// 프로젝트 전체 본인의 모든 일감 조회
	@Override
	public List<TaskProjectSelectVO> findMyProject(String uCode) {
		return taskMapper.myAllTasks(uCode);
	}

	// 전달된 상태값(commonId)이 "완료" 상태인지 확인 -민지-
	// (코드값 목록에서 defaultDescribe가 "완료"인 항목의 commonId와 비교)
	private boolean isCompletedStatus(String taskStatusCommonId) {
		if (taskStatusCommonId == null || taskStatusCommonId.isBlank()) {
			return false;
		}
		return taskMapper.taskStatuses().stream()
				.anyMatch(s -> "완료".equals(s.getDefaultDescribe()) && taskStatusCommonId.equals(s.getCommonId()));
	}

	// taskId 하위의 모든 일감(자식, 손자, ...) 중 진척도가 100%가 아닌 것만 재귀적으로 수집 -민지-
	// visited: 데이터가 잘못 얽혀 순환 참조가 생기는 경우를 대비한 방어 로직
	private void collectIncompleteDescendants(String taskId, List<TaskVO> acc, java.util.Set<String> visited) {
		if (!visited.add(taskId)) {
			return;
		}
		List<TaskVO> children = taskMapper.childTask(taskId);
		if (children == null || children.isEmpty()) {
			return;
		}
		for (TaskVO child : children) {
			boolean completed = child.getTaskProgress() != null && child.getTaskProgress() == 100L;
			if (!completed) {
				acc.add(child);
			}
			collectIncompleteDescendants(child.getTaskId(), acc, visited);
		}
	}

	// 일감 수정
	@Transactional(rollbackFor = Exception.class)
	@Override
	public void updateTask(TaskVO taskVO, List<MultipartFile> files, List<Long> deletedFileIds) throws Exception {
		
		// 진척도 관련 코드 추가 -민지-
		if (isCompletedStatus(taskVO.getTaskStatus())) {
			List<TaskVO> incompleteDescendants = new ArrayList<>();
			collectIncompleteDescendants(taskVO.getTaskId(), incompleteDescendants, new java.util.HashSet<>());
			if (!incompleteDescendants.isEmpty()) {
				throw new IllegalStateException("완료되지 않은 하위일감이 있어 이 일감을 완료 상태로 변경할 수 없습니다.");
			}
		}

		// [추가] 기존 첨부파일 삭제 처리 (S3 파일 삭제 및 DB 상태 변경)
		if (deletedFileIds != null && !deletedFileIds.isEmpty()) {
			for (Long fileId : deletedFileIds) {
				// 1. S3에 저장된 실제 파일 삭제를 위해 해당 file_id의 모든 버전 정보(savedName) 조회
				List<FileInfoVO> fileVersions = fileMapper.findFileInfoByFileId(fileId);

				if (fileVersions != null) {
					for (FileInfoVO fileInfo : fileVersions) {
						if (fileInfo.getSavedName() != null) {
							s3Service.deleteFile(fileInfo.getSavedName());
						}
					}
				}

				// 2. DB 업데이트: files 테이블의 is_deleted = 'Y'
				fileMapper.updateFileDeletedStatus(fileId);

				// 3. DB 업데이트: file_info(file_version) 테이블의 경로 및 크기 정보 NULL 처리
				fileMapper.clearFileVersionInfo(fileId);
			}
		}

		// 기존 일감 정보 업데이트
		taskMapper.updateTask(taskVO);

		// milestoneId 값이 있을 경우 -> 마일스톤 상태 변경하기 (진척도==100% -> closed, 진척도 < 100% ->
		// active)
		if (taskVO.getMilestoneId() != null) {
			syncMilestoneStatus(taskVO.getMilestoneId());
		}

		// 추가된 파일이 존재할 경우 업로드 및 버전 관리 진행
		if (files != null && !files.isEmpty()) {
			for (MultipartFile file : files) {
				if (!file.isEmpty()) {
					String originalFilename = file.getOriginalFilename();

					// 이제 is_deleted가 Y인 파일도 ID를 정상적으로 찾아옵니다.
					Long existingFileId = fileMapper.findFileId(taskVO.getTaskId(), originalFilename);

					Long targetFileId;

					if (existingFileId != null) {
						// 기존 파일이 있으면 ID를 유지
						targetFileId = existingFileId;

						// [핵심 추가] 혹시 is_deleted가 'Y' 상태일 수 있으므로 'N'으로 업데이트
						fileMapper.restoreFile(targetFileId);

					} else {
						// 아예 처음 올리는 새 파일이면 마스터 등록
						FileVO fileVO = new FileVO();
						fileVO.setTaskId(taskVO.getTaskId());
						fileVO.setFileName(originalFilename);

						fileMapper.insertFile(fileVO);
						targetFileId = fileVO.getFileId();
					}

					// 파일 저장 (S3)
					String savedName = UUID.randomUUID().toString() + "_" + originalFilename;
					s3Service.uploadFile(file, savedName);

					// 파일 버전(상세) 정보 등록
					FileInfoVO fileInfoVO = new FileInfoVO();
					fileInfoVO.setFileId(targetFileId);
					fileInfoVO.setFilePath(null); // 더 이상 로컬 경로 안 씀
					fileInfoVO.setFileSize(file.getSize());
					fileInfoVO.setUploader(taskVO.getUserCode());
					fileInfoVO.setSavedName(savedName); // S3 키

					// 여기서 (기존 MAX 버전 + 1) 로직이 타면서 자연스럽게 다음 버전으로 Insert 됩니다.
					fileMapper.insertFileInfo(fileInfoVO);
				}
			}
		}
	}

	// 일감 삭제
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteTask(String tId) {

		// 1. 삭제 전 해당 일감의 마일스톤 ID 조회
		Long milestoneId = taskMapper.getMilestoneIdByTaskId(tId);

		// 2. 일감 삭제
		taskMapper.deleteTask(tId);

		// 3. 마일스톤이 있었다면 상태 동기화
		if (milestoneId != null) {
			syncMilestoneStatus(milestoneId);
		}
	}

	// 댓글 목록 조회
	@Override
	public List<TaskCommentVO>findTaskComment(String tId) {
		return taskMapper.taskCommentList(tId);
	}

	// 댓글 등록
	@Transactional
	@Override
	public int insertTaskComment(TaskCommentVO commentVO) {
		System.out.println("들어온값:" + commentVO);
		return taskMapper.insertTaskComment(commentVO);
	}

	// 댓글 수정
	@Transactional
	@Override
	public int updateTaskComment(TaskCommentVO commentVO) {
		return taskMapper.updateTaskComment(commentVO);
	}

	// 댓글 삭제
	@Transactional
	@Override
	public int deleteTaskComment(Long commentId, String userCode) {
		return taskMapper.deleteTaskComment(commentId, userCode);
	}

	// 항목 변경이력
	@Override
	public List<TaskHistoryDTO> taskUpdateHistory(String tId) {
		List<TaskUpdateHistoryVO> list = taskMapper.taskUpdateHistory(tId);
		System.out.println("서비스쪽 : " + list);

		Map<Long, TaskHistoryDTO> hisMap = new LinkedHashMap<>();

		for (TaskUpdateHistoryVO value : list) {

			TaskHistoryDTO historyDTO = hisMap.get(value.getHistoryId());

			if (historyDTO == null) {
				historyDTO = new TaskHistoryDTO();
				historyDTO.setHistoryId(value.getHistoryId());
				historyDTO.setActionType(value.getActionType());
				historyDTO.setUserName(value.getUserName());
				historyDTO.setActionAt(value.getActionAt());
				historyDTO.setDetails(new ArrayList<>());

				hisMap.put(value.getHistoryId(), historyDTO);
			}

			TaskHistoryDetailDTO detailDTO = new TaskHistoryDetailDTO();
			detailDTO.setFieldName(value.getFieldName());
			detailDTO.setOldValue(value.getOldValue());
			detailDTO.setNewValue(value.getNewValue());

			historyDTO.getDetails().add(detailDTO);
		}

		return new ArrayList<>(hisMap.values());
	}

	// 소요시간
	@Override
	public List<TaskSpentTimeVO> taskSpentTime(String tId) {

		return taskMapper.taskSpentTime(tId);
	}

	private void syncMilestoneStatus(Long milestoneId) {
		if (milestoneId != null && milestoneId > 0) {
			milestoneMapper.updateMilestoneStatusByTaskProgress(milestoneId);
		}

	}

	@Override
	public List<TaskVO> findAllWithFilters(Map<String, Object> filterParams) {
		return taskMapper.findAllWithFilters(filterParams);
	}

	@Override
	public List<TaskVO> findAllMyTasksWithFilters(Map<String, Object> allParams) {

		return taskMapper.findAllMyTasksWithFilters(allParams);
	}

	@Override
	public List<TaskMemberVO> findAllMemberList() {

		return taskMapper.allMemberList();
	}

	// 권한 확인
	@Override
	public TaskPermissionVO getTaskPermissions(String userCode, Long pId) {
		TaskPermissionVO permissions = taskMapper.checkTaskPermissions(userCode, pId);

		// 권한 하나도 없어서 null 일때 타임리프에서 널포인터예외 뜨는거 빈객체 생성으로 방지
		if (permissions == null) {
			return new TaskPermissionVO();
		}

		return permissions;
	}

	@Override
	public int countAllWithFilters(Map<String, Object> params) {
		return taskMapper.countAllWithFilters(params);
	}

	@Override
	public int countAllList(Map<String, Object> params) {
		return taskMapper.countAllList(params);
	}

	@Override
	public int countAllMyTasksWithFilters(Map<String, Object> params) {
		return taskMapper.countAllMyTasksWithFilters(params);
	}

	@Override
	public FileDownloadDTO getFileForDownload(Long versionId) {
		return fileMapper.selectFileForDownload(versionId);
	}

	@Override
	public List<TaskTestCaseDTO> findTestCase(String tId, Long pId) {

		return taskMapper.taskTestCaseList(tId, pId);
	}

}
