package com.weple.cloud.testcase.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.weple.cloud.task.service.TaskParentVO;
import com.weple.cloud.testcase.service.CoverdStatusVO;
import com.weple.cloud.testcase.service.TestCaseMemberVO;
import com.weple.cloud.testcase.service.TestCasePriorityVO;
import com.weple.cloud.testcase.service.TestCaseVO;

public interface TestCaseMapper {
	
	public List<CoverdStatusVO>coverageStatus();
	
	public List<TestCaseMemberVO>testCaseMembers(@Param("pId") long pId);
	
	public List<TestCasePriorityVO>testCasePriorities(@Param("cId") long cId);

	public List<TaskParentVO> testCaseTaskList(@Param("pId") long pId);
	
	public int insertTestCase(TestCaseVO testCaseVO);
	
	public int getTestCaseCount(@Param("pId") long pId, @Param("searchKeyword") String searchKeyword);
	
	public List<TestCaseVO> testCaseList(@Param("pId") long pId, 
										@Param("searchKeyword") String searchKeyword, 
										@Param("offset") int offset, 
										@Param("pageSize") int pageSize);
	
	public TestCaseVO testCaseDetail(@Param("pId") long pId, @Param("tsId") String tsId);
	
	public int updateTestCase(TestCaseVO testCaseVO);
	
	public int deleteTestCase(@Param("testId") String testId, @Param("projectId") Long projectId);

}
