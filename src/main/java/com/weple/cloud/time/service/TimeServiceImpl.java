package com.weple.cloud.time.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.weple.cloud.time.mapper.TimeMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TimeServiceImpl implements TimeService {

	private final TimeMapper timeMapper;

	// -------------------------------프로젝트 내 소요시간------------------------------
	// 전체조회
	@Override
	public List<WorkTimeVO> findProjectTimeAll(Long projectId) {
		return timeMapper.projectTimeAll(projectId);
	}

	// 등록
	@Override
	public long addProjectTime(WorkTimeVO workTimeVO) {
		long result = timeMapper.insertProjectTime(workTimeVO);
		// 프로시저 실행
		if (result == 1) {
			timeMapper.updateTaskSpentHours();
		}
		return result == 1 ? workTimeVO.getWorkId() : -1;
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
