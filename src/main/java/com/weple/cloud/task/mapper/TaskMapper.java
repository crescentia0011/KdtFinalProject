package com.weple.cloud.task.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.weple.cloud.task.service.TaskCommentVO;
import com.weple.cloud.task.service.TaskMemberVO;
import com.weple.cloud.task.service.TaskMilestoneVO;
import com.weple.cloud.task.service.TaskParentVO;
import com.weple.cloud.task.service.TaskPermissionVO;
import com.weple.cloud.task.service.TaskPriorityVO;
import com.weple.cloud.task.service.TaskProjectSelectVO;
import com.weple.cloud.task.service.TaskSpentTimeVO;
import com.weple.cloud.task.service.TaskStatusVO;
import com.weple.cloud.task.service.TaskTypeListVO;
import com.weple.cloud.task.service.TaskUpdateHistoryVO;
import com.weple.cloud.task.service.TaskVO;

public interface TaskMapper {
	public List<TaskVO> selectAll(@Param("pId") long pId);
	
	public List<TaskTypeListVO> taskTypes(@Param("cId") long cId);
	
	public List<TaskStatusVO> taskStatuses();
	
	public List<TaskMemberVO> taskMembers(@Param("pId") long pId);
	
	public List<TaskPriorityVO> taskPriorities(@Param("cId") long cId);
	
	public List<TaskParentVO> taskParents(@Param("pId") long pId);
	
	public List<TaskMilestoneVO> taskMilestones(@Param("pId") long pId);
	
    public int insertTask(TaskVO taskVO);
    
    public List<TaskVO> selectAllList(Map<String,Object>allParams);
    
    public TaskVO taskDetail(@Param("tId") String tId);
    
    public List<TaskVO>childTask(@Param("tId") String tId);
    
    public List<TaskProjectSelectVO> myAllTasks(@Param("uCode") String uCode);
    
    public int updateTask(TaskVO taskVO);
    
    public void deleteTask(@Param("tId") String tId);
    
    public List<TaskCommentVO>taskCommentList(@Param("tId") String tId);
    
    public int insertTaskComment(TaskCommentVO taskCommentVO);
    
    public int updateTaskComment(TaskCommentVO taskCommentVo);
    
    public int deleteTaskComment(@Param("commentId") Long commentId , @Param("userCode") String userCode);
    
    public List<TaskUpdateHistoryVO> taskUpdateHistory(@Param("tId") String tId);
    
    public List<TaskSpentTimeVO> taskSpentTime(@Param("tId") String tId);

    public Long getMilestoneIdByTaskId(@Param("tId") String tId);
    
    public List<TaskVO> findAllWithFilters(Map<String, Object> filterParams);
    
    public List<TaskVO> findAllMyTasksWithFilters(Map<String,Object>allParams);
  
  // 소요시간의 진척도 자동계산 프로시저 - 민지
    public void updateHierarchicalProgress(@Param("taskId") String taskId);
    
    public List<TaskMemberVO> allMemberList();
    
    //일감 관련 권한 확인
    public TaskPermissionVO checkTaskPermissions(@Param("userCode") String userCode, @Param("pId") Long pId);
    
    // [내부 일감] 총 개수
    public int countAllWithFilters(Map<String, Object> params);

    // [전체 일감 - 관리자/소유자용] 총 개수
    public int countAllList(Map<String, Object> params);

    // [전체 일감 - 일반 멤버용] 총 개수
    public int countAllMyTasksWithFilters(Map<String, Object> params);

}
