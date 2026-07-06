package com.weple.cloud.testcase.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.weple.cloud.testcase.entity.TestCase;
import com.weple.cloud.testcase.service.TestCaseDetailProjection;

@Repository
public interface TestCaseRepository extends JpaRepository<TestCase, String> {

    // 테스트케이스 삭제
	void deleteByTestIdAndProjectId(String testId, Long projectId);
	
	// 상세 조회 (Native Query)
    @Query(value = """
            SELECT 
                tc.test_id            AS testId,
                tc.task_id            AS taskId,
                tc.milestone_version  AS milestoneVersion,
                tc.project_id         AS projectId,
                tc.user_code          AS userCode,
                tc.test_name          AS testName,
                tc.created_at         AS createdAt,
                tc.test_date          AS testDate,
                tc.priority           AS priority,
                tc.test_yn            AS testYn,
                tc.test_manager       AS testManager,
                tc.test_content       AS testContent,
                tc.test_describe      AS testDescribe,
                tc.coverage_status    AS coverageStatus,
                u.user_name           AS userName,    
                t.task_title          AS taskTitle,   
                NVL(m.user_name, '-') AS managerName 
            FROM test_case tc
            LEFT JOIN users u ON tc.user_code = u.user_code   
            LEFT JOIN users m ON tc.test_manager = m.user_code 
            LEFT JOIN task t ON tc.task_id = t.task_id
            WHERE tc.project_id = :projectId AND tc.test_id = :testId
            """, nativeQuery = true)
    Optional<TestCaseDetailProjection> findTestCaseDetail(@Param("projectId") Long projectId, @Param("testId") String testId);
    
    @Query(value = "SELECT MAX(TO_NUMBER(SUBSTR(test_id, 12))) " +
            "FROM test_case " +
            "WHERE test_id LIKE :prefix%", nativeQuery = true)
Integer findMaxSequenceByPrefix(@Param("prefix") String prefix);


@Query(value = """
     SELECT m_parent.milestone_title
     FROM task t
     JOIN milestone m_child ON t.milestone_id = m_child.milestone_id
     JOIN milestone m_parent ON m_child.parent_milestone_id = m_parent.milestone_id
     WHERE t.task_id = :taskId
     """, nativeQuery = true)
String findMilestoneTitleByTaskId(@Param("taskId") String taskId);

}