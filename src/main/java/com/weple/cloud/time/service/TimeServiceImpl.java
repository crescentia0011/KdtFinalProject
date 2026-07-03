package com.weple.cloud.time.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.weple.cloud.task.mapper.TaskMapper;
import com.weple.cloud.task.service.TaskVO;
import com.weple.cloud.time.mapper.TimeMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TimeServiceImpl implements TimeService {

	private final TimeMapper timeMapper;
	private final TaskMapper taskMapper;

	// -------------------------------프로젝트 내 소요시간------------------------------
	// 전체조회
	@Override
	public List<WorkTimeVO> findProjectTimeAll(Long projectId, String userCode) {
		return timeMapper.projectTimeAll(projectId, userCode);
	}

	// 단건 조회
	@Override
	public WorkTimeVO findProjectTimeOne(long workId) {
		return timeMapper.projectTimeOne(workId);
	}

	// 등록
	@Transactional
	@Override
	public long addProjectTime(WorkTimeVO workTimeVO) {
		// 작업분류를 "완료"로 등록하려는 경우, 진척도가 100%가 아니거나
		// 이 일감의 하위일감 중 미완료(100% 아님)가 하나라도 있으면 등록 자체를 막는다.
		if (timeMapper.countCompletedClassification(workTimeVO.getWorkName()) > 0) {
			if (workTimeVO.getProgress() == null || workTimeVO.getProgress() != 100L) {
				throw new IllegalStateException("진척도가 100%가 아니면 작업분류를 완료로 등록할 수 없습니다.");
			}
			List<TaskVO> incompleteDescendants = new java.util.ArrayList<>();
			collectIncompleteDescendants(workTimeVO.getTaskId(), incompleteDescendants, new java.util.HashSet<>());
			if (!incompleteDescendants.isEmpty()) {
				throw new IllegalStateException("완료되지 않은 하위일감이 있어 작업분류를 완료로 등록할 수 없습니다.");
			}
		}

		long result = timeMapper.insertProjectTime(workTimeVO);
		if (result > 0) {
			// 사용자가 입력한 진척도가 있으면(잠금 상태가 아니었으면) 이 일감 자신에게만 반영.
			// 상위 일감은 여기서 절대 건드리지 않는다 — 상위 일감의 진척도 "수정 가능 여부"는
			// /hasChildTask API(하위/하위의 하위 전체가 100%인지)가 프론트에서 판단해서
			// 잠금만 풀어줄 뿐, 값 자체를 자동으로 바꿔주지는 않는다.
			if (workTimeVO.getProgress() != null) {
				taskMapper.updateTaskProgress(workTimeVO.getTaskId(), workTimeVO.getProgress());
			}
	    }
		return result == 1 ? workTimeVO.getWorkId() : -1;
	}

	// taskId 하위의 모든 일감(자식, 손자, ...) 중 진척도가 100%가 아닌 것만 재귀적으로 수집
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

	// 수정
	@Override
	public Map<String, Object> modifyProjectTime(WorkTimeVO workTimeVO) {
		Map<String, Object> map = new java.util.HashMap<>();
		long result = timeMapper.updateProjectTime(workTimeVO);
		// 프로시저 실행
		if (result == 1) {
			timeMapper.updateTaskSpentHours();
		}
		map.put("result", result);
		return map;
	}

	// 삭제
	@Override
	public long removeProjectTime(long workId) {
		long result = timeMapper.deleteProjectTime(workId);
		//프로시저 실행
		if (result == 1) {
			timeMapper.updateTaskSpentHours();
		}
		return result;
	}

	// 일감별 소요시간 누적 합계 (프로시저)
	@Override
	public void updateTaskSpentHours() {
		timeMapper.updateTaskSpentHours();		
	}

}