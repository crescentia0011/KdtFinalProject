package com.weple.cloud.testcase.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.weple.cloud.task.service.TaskParentVO;
import com.weple.cloud.testcase.mapper.TestCaseMapper;
import com.weple.cloud.testcase.repository.TestCaseRepository;
import com.weple.cloud.testcase.service.CoverdStatusVO;
import com.weple.cloud.testcase.service.TestCaseDetailProjection;
import com.weple.cloud.testcase.service.TestCaseMemberVO;
import com.weple.cloud.testcase.service.TestCasePriorityVO;
import com.weple.cloud.testcase.service.TestCaseService;
import com.weple.cloud.testcase.service.TestCaseVO;
import java.time.LocalDate;
import com.weple.cloud.testcase.entity.TestCase;
import lombok.RequiredArgsConstructor;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;


@Service
@RequiredArgsConstructor
public class TestCaseServiceImpl implements TestCaseService{

	private final TestCaseMapper testCaseMapper;
	private final TestCaseRepository testCaseRepository;
	
	@Override
	public List<TaskParentVO> findTestCaseTaskList(Long pId) {

		return testCaseMapper.testCaseTaskList(pId);
	}

	@Override
	public List<CoverdStatusVO> findCoverageStatus() {

		return testCaseMapper.coverageStatus();
	}

	@Override
	public List<TestCaseMemberVO> findTestCaseMembers(Long pId) {

		return testCaseMapper.testCaseMembers(pId);
	}

	@Override
	public List<TestCasePriorityVO> findTestCasePriorities(Long cId) {

		return testCaseMapper.testCasePriorities(cId);
	}


	@Override
	public int getTestCaseCount(long pId, String searchKeyword) {
	    return testCaseMapper.getTestCaseCount(pId, searchKeyword);
	}

	@Override
	public List<TestCaseVO> findTestCaseList(long pId, String searchKeyword, int offset, int pageSize) {
	    return testCaseMapper.testCaseList(pId, searchKeyword, offset, pageSize);
	}

	@Override
    public TestCaseVO findTestCaseDetail(long pId, String tsId) {
        // JPA로 조회, 데이터가 없을 경우 처리 (예: 예외 발생 또는 null 반환)
        TestCaseDetailProjection projection = testCaseRepository.findTestCaseDetail(pId, tsId)
                .orElseThrow(() -> new IllegalArgumentException("해당 테스트 케이스가 존재하지 않습니다."));
        
        // Projection 결과를 기존 Controller에서 쓰던 VO로 매핑
        TestCaseVO vo = new TestCaseVO();
        vo.setTestId(projection.getTestId());
        vo.setTaskId(projection.getTaskId());
        vo.setMilestoneVersion(projection.getMilestoneVersion());
        vo.setProjectId(projection.getProjectId());
        vo.setUserCode(projection.getUserCode());
        vo.setTestName(projection.getTestName());
        if (projection.getCreatedAt() != null) {
            vo.setCreatedAt(java.sql.Timestamp.valueOf(projection.getCreatedAt()));
        }

        if (projection.getTestDate() != null) {
            vo.setTestDate(java.sql.Date.valueOf(projection.getTestDate()));
        }
        vo.setPriority(projection.getPriority());
        vo.setTestYn(projection.getTestYn());
        vo.setTestManager(projection.getTestManager());
        vo.setTestContent(projection.getTestContent());
        vo.setTestDescribe(projection.getTestDescribe());
        vo.setCoverageStatus(projection.getCoverageStatus());
        
        // 조인 데이터
        vo.setUserName(projection.getUserName());
        vo.setTaskTitle(projection.getTaskTitle());
        vo.setManagerName(projection.getManagerName());
        
        return vo;
    }
	
	@Override
	@Transactional 
	public void updateTestCaseService(TestCaseVO testCaseVO) {
        
		// 1. 기존 데이터 조회
		TestCase testCase = testCaseRepository.findById(testCaseVO.getTestId())
				.orElseThrow(() -> new RuntimeException("테스트 케이스 수정에 실패했습니다. (대상 ID 없음)"));
		
		// 2. VO의 java.util.Date를 Entity의 LocalDate로 변환
		LocalDate localTestDate = null;
		if (testCaseVO.getTestDate() != null) {
			localTestDate = new java.sql.Date(testCaseVO.getTestDate().getTime()).toLocalDate();
		}
		
		// 3. 엔티티 데이터 수정
		testCase.update(
				testCaseVO.getTaskId(),
				testCaseVO.getMilestoneVersion(),
				testCaseVO.getTestName(),
				localTestDate, 
				testCaseVO.getPriority(),
				testCaseVO.getTestYn(),
				testCaseVO.getTestManager(),
				testCaseVO.getTestContent(),
				testCaseVO.getTestDescribe(),
				testCaseVO.getCoverageStatus()
		);
	}
	
	@Override
    @Transactional
    public void deleteTestCaseService(String testId, Long projectId) {
        testCaseRepository.deleteByTestIdAndProjectId(testId, projectId);
    }
	
	
	@Override
	@Transactional
	public int insertTestCaseService(TestCaseVO vo) {
	    
	    // 커스텀 ID 생성 로직 (TST-YYMMDD_시퀀스)
	    String todayStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
	    String idPrefix = "TST-" + todayStr + "_";
	    
	    Integer maxSeq = testCaseRepository.findMaxSequenceByPrefix(idPrefix);
	    int nextSeq = (maxSeq == null) ? 1 : maxSeq + 1;
	    String newTestId = idPrefix + nextSeq;

	    //  부모 마일스톤 타이틀 조회
	    String milestoneVersion = testCaseRepository.findMilestoneTitleByTaskId(vo.getTaskId());

	    LocalDate testDateLocal = null;
	    if (vo.getTestDate() != null) {
	        testDateLocal = vo.getTestDate().toInstant()
	                                  .atZone(ZoneId.systemDefault())
	                                  .toLocalDate();
	    }

	    // Entity 빌드 및 저장
	    TestCase testCase = TestCase.builder()
	            .testId(newTestId)
	            .taskId(vo.getTaskId())
	            .milestoneVersion(milestoneVersion)
	            .projectId(vo.getProjectId())
	            .userCode(vo.getUserCode())
	            .testName(vo.getTestName())
	            // createdAt은 @PrePersist가 자동 처리하므로 생략
	            .testDate(testDateLocal)
	            .priority(vo.getPriority())
	            .testYn(vo.getTestYn())
	            .testManager(vo.getTestManager())
	            .testContent(vo.getTestContent())
	            .testDescribe(vo.getTestDescribe())
	            .coverageStatus(vo.getCoverageStatus())
	            .build();

	    testCaseRepository.save(testCase);

	    return 1; 
	}

}