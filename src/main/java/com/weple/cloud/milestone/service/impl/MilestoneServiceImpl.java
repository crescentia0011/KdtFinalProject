package com.weple.cloud.milestone.service.impl;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.weple.cloud.milestone.mapper.MilestoneMapper;
import com.weple.cloud.milestone.service.MilestoneDetailVO;
import com.weple.cloud.milestone.service.MilestoneInfoVO;
import com.weple.cloud.milestone.service.MilestoneService;
import com.weple.cloud.milestone.service.MilestoneVO;
import com.weple.cloud.milestone.service.TaskGroupStatVO;
import com.weple.cloud.milestone.service.TaskStatDTO;
import com.weple.cloud.project.service.ProjectVO;
import com.weple.cloud.system.service.TaskTypeVO;
import com.weple.cloud.task.service.TaskVO;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MilestoneServiceImpl implements MilestoneService {

	private final MilestoneMapper milestoneMapper;
	
	@Override
	public List<MilestoneInfoVO> getRoadmapList(Long projectId) {
	    // 1. DB에서 마일스톤과 일감 통계를 독립적으로 아주 빠르게 조회
	    List<MilestoneVO> flatMilestones = milestoneMapper.selectFlatMilestones(projectId);
	    List<TaskStatDTO> taskStats = milestoneMapper.selectTaskStatsFlat(projectId);
	    
	    if (flatMilestones.isEmpty()) return new ArrayList<>();

	    // 2. 일감 통계를 Map 구조(Key: milestoneId)로 맵핑
	    Map<Long, TaskStatDTO> statMap = taskStats.stream()
	            .collect(Collectors.toMap(TaskStatDTO::getMilestoneId, stat -> stat));

	    LocalDate today = LocalDate.now();

	    // 3. MilestoneVO -> MilestoneInfoVO 변환 및 하위 마일스톤 통계/지연일 계산
	    List<MilestoneInfoVO> infoList = flatMilestones.stream().map(vo -> {
	        MilestoneInfoVO info = new MilestoneInfoVO();
	        
	        // 기존 필드 복사 (이름이 일치하는 필드 자동 복사)
	        info.setMilestoneId(vo.getMilestoneId());
	        info.setProjectId(vo.getProjectId());
	        info.setUserCode(vo.getUserCode());
	        info.setMilestoneTitle(vo.getMilestoneTitle());
	        info.setMilestoneDescribe(vo.getMilestoneDescribe());
	        info.setFinishDate(vo.getFinishDate());
	        info.setMilestoneStatus(vo.getMilestoneStatus());
	        info.setCreatedAt(vo.getCreatedAt());
	        info.setUpdatedAt(vo.getUpdatedAt());
	        info.setParentMilestoneId(vo.getParentMilestoneId());
	        info.setChildMilestones(new ArrayList<>()); // 리스트 초기화

	        // 일감 통계 바인딩 (하위 마일스톤에 해당)
	        TaskStatDTO stat = statMap.get(vo.getMilestoneId());
	        if (stat != null) {
	            info.setTotalTaskCount(stat.getTotalTaskCount());
	            info.setClosedTaskCount(stat.getClosedTaskCount());
	            info.setProgressPercentage(stat.getProgressPercentage());
	        }

	        // 하위 마일스톤 자체 지연일 계산 (진척도 100% 미만이고 마감일이 지났다면)
	        if (info.getProgressPercentage() < 100 && info.getFinishDate() != null && today.isAfter(info.getFinishDate())) {
	            info.setDelayDays((int) ChronoUnit.DAYS.between(info.getFinishDate(), today));
	        }
	        return info;
	    }).collect(Collectors.toList());

	    // 4. 자바 메모리상에서 계층 트리(Version -> Milestone) 구성
	    Map<Long, MilestoneInfoVO> allMap = infoList.stream()
	            .collect(Collectors.toMap(MilestoneInfoVO::getMilestoneId, info -> info));

	    List<MilestoneInfoVO> versions = new ArrayList<>();

	    for (MilestoneInfoVO item : infoList) {
	        if (item.getParentMilestoneId() == null) {
	            versions.add(item); // 최상위 버전(부모)
	        } else {
	            // 부모 버전을 찾아 자식 리스트에 추가
	            MilestoneInfoVO parent = allMap.get(item.getParentMilestoneId().longValue());
	            if (parent != null) {
	                parent.getChildMilestones().add(item);
	            }
	        }
	    }

	    // 5. 상위 버전(부모)의 통계 데이터 롤업 계산
	    for (MilestoneInfoVO parent : versions) {
	        List<MilestoneInfoVO> children = parent.getChildMilestones();
	        if (!children.isEmpty()) {
	            // 하위 마일스톤 총 개수 및 완료(g2) 개수 계산
	            parent.setTotalMilestoneCount(children.size());
	            long closedCount = children.stream()
	                    .filter(c -> "g2".equals(c.getMilestoneStatus())).count();
	            parent.setClosedMilestoneCount((int) closedCount);

	            // 부모의 진척도 = 하위 마일스톤들의 일감 진척도 평균값
	            double avgProgress = children.stream()
	                    .mapToInt(MilestoneInfoVO::getProgressPercentage).average().orElse(0.0);
	            parent.setProgressPercentage((int) Math.round(avgProgress));

	            // 부모 버전 자체의 지연일 계산
	            if (parent.getProgressPercentage() < 100 && parent.getFinishDate() != null && today.isAfter(parent.getFinishDate())) {
	                parent.setDelayDays((int) ChronoUnit.DAYS.between(parent.getFinishDate(), today));
	            }
	        }
	    }

	    // 최신 생성 버전이 위로 오도록 정렬하여 반환
	    versions.sort((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()));
	    return versions;
	}

	// 마일스톤 상세조회
	@Override
    public MilestoneDetailVO getMilestoneDetailInfo(Long projectId, Long milestoneId) {
        // 기본 정보 및 시간 총합 가져오기
        MilestoneDetailVO detailVO = milestoneMapper.selectMilestoneDetailBase(projectId, milestoneId);
        
        if (detailVO == null) {
            throw new IllegalArgumentException("존재하지 않는 마일스톤입니다.");
        }

        // 4대 분류 통계 리스트 세팅
        detailVO.setStatusStats(calculatePercentages(milestoneMapper.selectTaskStatusStats(projectId, milestoneId)));
        detailVO.setPriorityStats(calculatePercentages(milestoneMapper.selectTaskPriorityStats(projectId, milestoneId)));
        detailVO.setTypeStats(calculatePercentages(milestoneMapper.selectTaskTypeStats(projectId, milestoneId)));
        detailVO.setManagerStats(calculatePercentages(milestoneMapper.selectTaskManagerStats(projectId, milestoneId)));

        return detailVO;
    }

	// 마일스톤 상세페이지 연결된 일감 불러오기
    @Override
    public List<TaskVO> getMilestoneTasksWithPaging(Long projectId, Long milestoneId, int page, int pageSize) {
        int startRow = (page - 1) * pageSize + 1;
        int endRow = page * pageSize;
        return milestoneMapper.selectMilestoneTasksWithPaging(projectId, milestoneId, startRow, endRow);
    }

    // 진척도 백분율(%) 산출 공통 편의 메서드
    private List<TaskGroupStatVO> calculatePercentages(List<TaskGroupStatVO> stats) {
        for (TaskGroupStatVO stat : stats) {
            if (stat.getTotalCount() > 0) {
                int percentage = Math.round(((float) stat.getClosedCount() / stat.getTotalCount()) * 100);
                stat.setProgressPercentage(percentage);
            } else {
                stat.setProgressPercentage(0);
            }
        }
        return stats;
    }
    
    @Override
    @Transactional // 마일스톤 수정, 일감 해제, 일감 등록이 동시에 일어나므로 트랜잭션 필수
    public void modifyMilestoneTasks(MilestoneVO milestoneVO, List<String> taskIds) {
                
        // 1. 기존에 이 마일스톤에 연결되어 있던 일감들을 전부 해제(NULL)
        milestoneMapper.clearTasksMilestoneId(milestoneVO.getMilestoneId());
        
        // 2. 새로 최신화된 일감 리스트가 있다면 일괄 연결 
        if (taskIds != null && !taskIds.isEmpty()) {
            milestoneMapper.updateTasksMilestoneId(milestoneVO.getProjectId(), milestoneVO.getMilestoneId(), taskIds);
        }
    }
	
    
    // 단건 조회
 	@Override
 	public ProjectVO findById(Long projectId) {
 		return milestoneMapper.selectById(projectId);
 	}
    
    
 	// 버전 등록
    @Override
    public void addVersion(MilestoneVO milestoneVO) {

    	milestoneMapper.insertVersion(milestoneVO);
    }

    // 마일스톤 등록
    @Transactional // [핵심] 두 작업을 하나의 단위로 묶어 무결성 보장
    @Override
    public void addMilestone(MilestoneVO milestoneVO, List<String> taskIds) {
        
        // 1. 마일스톤 데이터 저장 (실행 완료 후 milestoneVO 내부에 새 milestoneId가 자동 주입됨)
        milestoneMapper.insertMilestone(milestoneVO);
        
        // 2. 화면에서 체크하여 넘어온 일감 ID 목록이 존재할 경우에만 벌크 업데이트 실행
        if (taskIds != null && !taskIds.isEmpty()) {
            milestoneMapper.updateTasksMilestoneId(
                milestoneVO.getProjectId(),
                milestoneVO.getMilestoneId(), 
                taskIds
            );
        }
    }
    
	// 상위 마일스톤 조회
	@Override
	public List<MilestoneVO> getMilestoneListByProjectId(Long projectId) {
	    return milestoneMapper.selectMilestoneListByProjectId(projectId);
	}
	
	@Override
    public List<TaskTypeVO> getTaskTypeList() {
        return milestoneMapper.selectTaskTypeList();
    }

    @Override
    public List<TaskVO> getUnassignedTaskList(Long projectId, Long milestoneId, int startRow, int endRow, String taskStatus, String priority, String taskManager, Long typeId) {
        return milestoneMapper.selectUnassignedTasks(projectId, milestoneId, startRow, endRow, taskStatus, priority, taskManager, typeId);
    }

    @Override
    public int getUnassignedTaskCount(Long projectId, Long milestoneId, String taskStatus, String priority, String taskManager, Long typeId) {
        return milestoneMapper.selectUnassignedTasksCount(projectId, milestoneId, taskStatus, priority, taskManager, typeId);
    }

    @Override
    public MilestoneVO getMilestoneInfoById(Long milestoneId) {
        return milestoneMapper.selectMilestoneInfoById(milestoneId);
    }
    
    
    @Override
    public List<TaskVO> getConnectedTaskList(Long milestoneId) {
        return milestoneMapper.selectConnectedTaskList(milestoneId);
    }
    
    @Override
    public List<MilestoneVO> getMilestoneListForUpdate(Long projectId, Long milestoneId) {
        return milestoneMapper.selectMilestoneListForUpdate(projectId, milestoneId);
    }
    
    @Override
    @Transactional // 마일스톤 수정, 일감 해제, 일감 등록이 동시에 일어나므로 트랜잭션 필수
    public void modifyMilestone(MilestoneVO milestoneVO, List<String> taskIds) {
        // 1. 마일스톤 기본 정보 수정 (이름, 설명, 완료일, 상위 마일스톤)
        milestoneMapper.updateMilestone(milestoneVO);
        
        // 2. [A안] 기존에 이 마일스톤에 연결되어 있던 일감들을 전부 해제(NULL)
        milestoneMapper.clearTasksMilestoneId(milestoneVO.getMilestoneId());
        
        // 3. [A안] 새로 최신화된 일감 리스트가 있다면 일괄 연결 (기존 updateTasksMilestoneId 재사용)
        if (taskIds != null && !taskIds.isEmpty()) {
            milestoneMapper.updateTasksMilestoneId(milestoneVO.getProjectId(), milestoneVO.getMilestoneId(), taskIds);
        }
    }
    
    
	// 마일스톤 편집
	@Override
	public void updateParentMilestone(MilestoneVO milestoneVO) {
		milestoneMapper.updateParentMilestone(milestoneVO);
	}

	// 마일스톤 삭제
	@Override
	public int deleteMilestone(Long milestoneId) {
		return milestoneMapper.deleteMilestone(milestoneId);
	}

}
