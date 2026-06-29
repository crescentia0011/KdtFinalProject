package com.weple.cloud.task.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.weple.cloud.file.FileDownloadDTO;

public interface TaskService {
	public List<TaskVO> findAll(Long pId);
	
	public List<TaskTypeListVO> findType(Long cId);
	
	public List<TaskStatusVO> findStatus();
	
	public List<TaskMemberVO> findMember(Long pId);
	
	public List<TaskPriorityVO> findPriority(Long cId);
	
	public List<TaskParentVO> findParent(Long pId);
	
	public List<TaskMilestoneVO> findMilestone(Long pId);
	
	public int insertTask(TaskVO taskVO , List<MultipartFile> files)throws Exception;
	
	public TaskVO findTaskDetail(String tId);
	
	public List<TaskVO> findChildTask(String tId);
	
	public List<TaskVO> findAllList(Map<String, Object> allParams);
	
	public List<TaskProjectSelectVO> findMyProject(String uCode);
	
	void updateTask(TaskVO taskVO, List<MultipartFile> files, List<Long> deletedFileIds) throws Exception;
	
	public void deleteTask(String tId);
	
	public List<TaskCommentVO> findTaskComment(String tId);
	
	public int insertTaskComment(TaskCommentVO commentVO);

	public int updateTaskComment(TaskCommentVO commentVO);

	public int deleteTaskComment(Long commentId ,String userCode);
	
	//실제로 내보내는 값은 DTO 큰틀과 안에 있는 상세 list
	public List<TaskHistoryDTO> taskUpdateHistory(String tId);
    
    public List<TaskSpentTimeVO> taskSpentTime(String tId);
    
    public List<TaskVO> findAllWithFilters(Map<String, Object> filterParams);

	public List<TaskVO> findAllMyTasksWithFilters(Map<String, Object> allParams);
	
	public List<TaskMemberVO> findAllMemberList();
	
	public TaskPermissionVO getTaskPermissions(String userCode, Long pId);
	
	// [내부 일감] 총 개수
    public int countAllWithFilters(Map<String, Object> params);

    // [전체 일감 - 관리자/소유자용] 총 개수
    public int countAllList(Map<String, Object> params);

    // [전체 일감 - 일반 멤버용] 총 개수
    public int countAllMyTasksWithFilters(Map<String, Object> params);
    
    // 다운로드 관련 정보
    public FileDownloadDTO getFileForDownload(Long versionId);
}
