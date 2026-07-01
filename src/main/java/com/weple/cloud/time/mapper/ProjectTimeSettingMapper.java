package com.weple.cloud.time.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.weple.cloud.system.service.CodeValueVO;

public interface ProjectTimeSettingMapper {

    // 회사에서 사용중(using_yn = 'Y')인 작업분류 목록 (시간추적 사용 선택 대상)
    List<CodeValueVO> findUsingWorkClassifications(@Param("companyId") Long companyId);

    // 프로젝트가 이미 사용 선택해둔 작업분류 ID 목록
    List<String> findSelectedClassificationIds(@Param("projectId") Long projectId);

    long deleteSelectionByProject(@Param("projectId") Long projectId);

    long insertSelection(@Param("projectId") Long projectId,
                          @Param("taskClassificationId") String taskClassificationId);
}