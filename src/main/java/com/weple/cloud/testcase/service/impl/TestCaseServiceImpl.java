package com.weple.cloud.testcase.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.weple.cloud.task.service.TaskParentVO;
import com.weple.cloud.testcase.mapper.TestCaseMapper;
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
	public int insetTestCaseService(TestCaseVO TestCaseVO) {
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
	    // 프로젝트 ID까지 조건으로 주어 다른 프로젝트의 글이 삭제되는 보안 사고 방지
	    int result = testCaseMapper.deleteTestCase(testId, projectId);
	    
	    if (result == 0) {
	        throw new RuntimeException("삭제할 테스트 케이스를 찾을 수 없거나 삭제 권한이 없습니다.");
	    }
	}

}
