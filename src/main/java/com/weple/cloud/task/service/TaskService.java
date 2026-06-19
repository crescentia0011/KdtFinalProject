package com.weple.cloud.task.service;

import java.util.List;

public interface TaskService {
	public List<TaskVO> findAll();
	
	public List<TaskTypeListVO> findType();
	
	public List<TaskStatusVO> findStatus();
	
	public List<TaskMemberVO> findMember(Integer pId);
	
	public List<TaskParentVO> findParent();
	
	public int insertTask(TaskVO taskVO);
}
