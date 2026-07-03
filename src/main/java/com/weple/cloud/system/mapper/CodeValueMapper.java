package com.weple.cloud.system.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.weple.cloud.system.service.CodeValueVO;

public interface CodeValueMapper {
	// -------------------------------코드값------------------------------
	// 전체조회
	public List<CodeValueVO> selectCodeValueAll(long companyId);
	
	// 상세조회
	public CodeValueVO selectCodeValueInfo(Map<String, Object> map);

	// 등록
	public long insertCodeValue(Map<String, Object> map);

	// 수정 시 나 제외하고 모든 기본값을 N으로 변경
	public void resetDefaultYn(Map<String, Object> params);
	
	// 추가 등록 시 모든 기본값을 N으로 변경
	public void resetAllDefaultYn(String type);
	
	// 수정 (데이터 1개씩 수정 가능)
	public long updateCodeValue(CodeValueVO codeValueVO);
	
	// 삭제
	public void deleteCodeValue(Map<String, Object> params);
	
	// 수정 (드래그 앤 드랍으로 순서 변경한 데이터 저장)
//	public void updateOrderNum(@Param("type") String type, @Param("id") String id, @Param("orderNum") long orderNum);
	
	// 기본값인 항목의 이름을 조회
	String findDefaultNameByType(@Param("type") String type, @Param("cno") String cno);
	
	// 드래그앤드랍
    public void insertCodeValueReorder(@Param("type") String type, @Param("item") CodeValueVO item);
    public void updateCodeOrder(@Param("list") List<CodeValueVO> list);
}