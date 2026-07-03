package com.weple.cloud.testcase.repository;

import com.weple.cloud.testcase.entity.TestCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestCaseRepository extends JpaRepository<TestCase, String> {

    // 테스트케이스 삭제
	void deleteByTestIdAndProjectId(String testId, Long projectId);
	
	

}