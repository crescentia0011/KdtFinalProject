package com.weple.cloud.testcase.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.weple.cloud.task.service.TaskParentVO;
import com.weple.cloud.testcase.service.CoverdStatusVO;
import com.weple.cloud.testcase.service.TestCaseMemberVO;
import com.weple.cloud.testcase.service.TestCasePriorityVO;

public interface TestCaseMapper {
	
	public List<CoverdStatusVO>coverageStatus();
	
	public List<TestCaseMemberVO>testCaseMembers(@Param("pId") long pId);
	
	public List<TestCasePriorityVO>testCasePriorities(@Param("cId") long cId);

	public List<TaskParentVO> testCaseTaskList(@Param("pId") long pId);

}
