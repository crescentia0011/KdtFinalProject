package com.weple.cloud.testcase.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.weple.cloud.task.service.TaskParentVO;
import com.weple.cloud.testcase.mapper.TestCaseMapper;
import com.weple.cloud.testcase.repository.TestCaseRepository;
import com.weple.cloud.testcase.service.CoverdStatusVO;
import com.weple.cloud.testcase.service.TestCaseMemberVO;
import com.weple.cloud.testcase.service.TestCasePriorityVO;
import com.weple.cloud.testcase.service.TestCaseService;
import com.weple.cloud.testcase.service.TestCaseVO;

import lombok.RequiredArgsConstructor;

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
	public int insertTestCaseService(TestCaseVO TestCaseVO) {
		return testCaseMapper.insertTestCase(TestCaseVO);
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
		
		return testCaseMapper.testCaseDetail(pId, tsId);
	}
	
	@Override
    @Transactional
    public void updateTestCaseService(TestCaseVO testCaseVO) {
        int result = testCaseMapper.updateTestCase(testCaseVO);
        
        if (result == 0) {
            throw new RuntimeException("테스트 케이스 수정에 실패했습니다. (대상 ID 없음)");
        }
    }
	
	@Override
    @Transactional
    public void deleteTestCaseService(String testId, Long projectId) {
        testCaseRepository.deleteByTestIdAndProjectId(testId, projectId);
    }

}