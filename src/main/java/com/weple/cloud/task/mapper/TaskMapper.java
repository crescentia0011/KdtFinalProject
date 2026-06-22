package com.weple.cloud.task.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.weple.cloud.task.service.TaskMemberVO;
import com.weple.cloud.task.service.TaskMilestoneVO;
import com.weple.cloud.task.service.TaskParentVO;
import com.weple.cloud.task.service.TaskPriorityVO;
import com.weple.cloud.task.service.TaskProjectSelectVO;
import com.weple.cloud.task.service.TaskStatusVO;
import com.weple.cloud.task.service.TaskTypeListVO;
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
    
    public List<TaskVO> selectAllList(@Param("tManager") String tManager);
    
    public List<TaskProjectSelectVO> myAllTasks(@Param("uCode") String uCode);

}
