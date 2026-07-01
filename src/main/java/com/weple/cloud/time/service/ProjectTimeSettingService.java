package com.weple.cloud.time.service;

import java.util.List;

import com.weple.cloud.system.service.CodeValueVO;

public interface ProjectTimeSettingService {

    // 회사에서 사용중인 작업분류 목록 (체크박스로 보여줄 대상)
    List<CodeValueVO> findClassificationOptions(Long companyId);

    // 프로젝트가 이미 사용 선택해둔 작업분류 ID 목록
    List<String> findSelectedClassificationIds(Long projectId);

    // 프로젝트의 사용 선택 값을 통째로 다시 저장함 (삭제 후 재등록)
    void saveSelectedClassifications(Long projectId, List<String> taskClassificationIds);
}