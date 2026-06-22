package com.weple.cloud.task.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.weple.cloud.task.mapper.TaskMapper;
import com.weple.cloud.task.service.TaskMemberVO;
import com.weple.cloud.task.service.TaskMilestoneVO;
import com.weple.cloud.task.service.TaskParentVO;
import com.weple.cloud.task.service.TaskPriorityVO;
import com.weple.cloud.task.service.TaskProjectSelectVO;
import com.weple.cloud.task.service.TaskService;
import com.weple.cloud.task.service.TaskStatusVO;
import com.weple.cloud.task.service.TaskTypeListVO;
import com.weple.cloud.task.service.TaskVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {
	
	private final TaskMapper taskMapper;
	
	//내부 일감 전체 조회
	@Override
	public List<TaskVO> findAll(Long pId) {
		return taskMapper.selectAll(pId);
	}
	//일감유형 목록 조회
	@Override
	public List<TaskTypeListVO> findType(Long cId) {
		return taskMapper.taskTypes(cId);
	}
	//일감 상태 목록 조회
	@Override
	public List<TaskStatusVO> findStatus() {
		return taskMapper.taskStatuses();
	}
	//프로젝트 참여자 목록 조회 담당자 지정
	@Override
	public List<TaskMemberVO> findMember(Long pId) {
		return taskMapper.taskMembers(pId);
	}
	//우선순위 목록 조회
	@Override
	public List<TaskPriorityVO> findPriority(Long cId) {
		return taskMapper.taskPriorities(cId);
	}
	//상위 일감 목록 조회
	@Override
	public List<TaskParentVO> findParent(Long pId) {
		return taskMapper.taskParents(pId);
	}
	// 마일스톤 목록 조회
	@Override
	public List<TaskMilestoneVO> findMilestone(Long pId) {
		return taskMapper.taskMilestones(pId);
	}
	// 등록 쿼리
	@Override
	@Transactional
    public int insertTask(TaskVO taskVO) {
        return taskMapper.insertTask(taskVO);
    }
	// 전체 일감 조회
	@Override
	public List<TaskVO> findAllList(String tManager) {
		
		return taskMapper.selectAllList(tManager);
	}
	// 프로젝트 전체 본인의 모든 일감 조회
	@Override
	public List<TaskProjectSelectVO> findMyProject(String uCode) {
		return taskMapper.myAllTasks(uCode);
	}




}
