package com.weple.cloud.system.service;

import java.util.List;
import java.util.Map;

import org.springframework.transaction.annotation.Transactional;

public interface CodeValueService {
	// -------------------------------코드값------------------------------
	// 전체조회
	public List<CodeValueVO> findCodeValueAll(long companyId);
	
	// 상세조회
	public CodeValueVO findCodeValueInfo(CodeValueVO codeValueVO, String type);

	// 등록
	public long addCodeValue(CodeValueVO codeValueVO, String type);

	// [프로시저] 등록 - 기본값 처리 포함 (SP_ADD_CODE_VALUE)
	public String addCodeValueByProc(CodeValueVO codeValueVO, String type);
	
	// 수정 (모든 기본값을 N으로 변경)
	public void resetDefaultYn(Map<String, Object> params);
	
	// 추가 등록 시 모든 기본값을 N으로 변경
	public void resetAllDefaultYn(String type);
	
	// 수정 (데이터 1개씩 수정 가능)
	public void modifyCodeValue(CodeValueVO codeValueVO, String type);

	// [프로시저] 수정 - 사용여부/기본값 처리 포함 (SP_UPDATE_CODE_VALUE)
	public void modifyCodeValueByProc(CodeValueVO codeValueVO, String type);
	
	// 삭제
	public void removeCodeValue(String type, String id);
	
	// 수정 (드래그 앤 드랍으로 순서 변경한 데이터 저장)
	// public void updateOrder(String type, String cno, long orderNum);
	
	//디폴트 이름 불러오기
	public String findDefaultNameByType(String type, String cno);
	
	//드래그앤드랍
	@Transactional
	public void reorderCodes(String type, List<CodeValueVO> itemList);
}