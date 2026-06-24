package com.weple.cloud.system.service;

import java.util.List;
import java.util.Map;
public interface CodeValueService {
	// -------------------------------코드값------------------------------
	// 전체조회
	public List<CodeValueVO> findCodeValueAll();
	
	// 상세조회
	public CodeValueVO findCodeValueInfo(CodeValueVO codeValueVO, String type);

	// 등록
	public long addCodeValue(CodeValueVO codeValueVO, String type);
	
	// 수정 (모든 기본값을 N으로 변경)
	public void resetDefaultYn(Map<String, Object> params);
	
	// 추가 등록 시 모든 기본값을 N으로 변경
	public void resetAllDefaultYn(String type);
	
	// 수정 (데이터 1개씩 수정 가능)
	public void modifyCodeValue(CodeValueVO codeValueVO, String type);
	
	// 수정 (드래그 앤 드랍으로 순서 변경한 데이터 저장)
	// public void updateOrder(String type, String cno, long orderNum);
}
