package com.weple.cloud.task.mapper;

import java.util.List;

import com.weple.cloud.task.service.TaskMemberVO;
import com.weple.cloud.task.service.TaskParentVO;
import com.weple.cloud.task.service.TaskStatusVO;
import com.weple.cloud.task.service.TaskTypeListVO;
import com.weple.cloud.task.service.TaskVO;

public interface TaskMapper {
	public List<TaskVO> selectAll();
	
	public List<TaskTypeListVO> taskTypes();
	
	public List<TaskStatusVO> taskStatuses();
	
	public List<TaskMemberVO> taskMembers(Integer pId);
	
	public List<TaskParentVO> taskParents();
	
    public int insertTask(TaskVO taskVO);

}
