package com.weple.cloud.testcase.service;

import java.util.List;
import com.weple.cloud.task.service.TaskParentVO;

public interface TestCaseService {
	
	
	public List<CoverdStatusVO>findCoverageStatus();
	
	public List<TestCaseMemberVO>findTestCaseMembers(Long pId);
	
	public List<TestCasePriorityVO>findTestCasePriorities(Long cId);

	public List<TaskParentVO> findTestCaseTaskList(Long pId);
	
	public int insetTestCaseService(TestCaseVO TestCaseVO);
	
	public int getTestCaseCount(long pId, String searchKeyword);

	public List<TestCaseVO> findTestCaseList(long pId, String searchKeyword, int offset, int pageSize);

}
