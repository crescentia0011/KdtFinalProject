package com.weple.cloud.system.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.weple.cloud.system.mapper.CodeValueMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CodeValueServiceImpl implements CodeValueService {

	private final CodeValueMapper codeValueMapper;

	// -------------------------------코드값------------------------------
	// 전체조회
	@Override
	public List<CodeValueVO> findCodeValueAll() {
		return codeValueMapper.selectCodeValueAll();
	}

	// 상세조회
	@Override
	public CodeValueVO findCodeValueInfo(CodeValueVO codeValueVO, String type) {
		Map<String, Object> map = new HashMap<>();
	    map.put("vo", codeValueVO);
	    map.put("type", type);

	    return codeValueMapper.selectCodeValueInfo(map);
	}

	// 등록
	@Override
	public long addCodeValue(CodeValueVO codeValueVO, String type) {
		if ("Y".equals(codeValueVO.getDefaultYn())) {
	        codeValueMapper.resetAllDefaultYn(type); 
	    }
		Map<String, Object> map = new HashMap<>();
		map.put("vo", codeValueVO);
		map.put("type", type);
	    return codeValueMapper.insertCodeValue(map);
	}

	// 수정 (데이터 1개씩 수정 가능)
	@Override
	public void modifyCodeValue(CodeValueVO codeValueVO, String type) {
		if ("N".equals(codeValueVO.getUsingYn())) {
	        codeValueVO.setDefaultYn("N");
	    }

	    if ("Y".equals(codeValueVO.getDefaultYn())) {
	        Map<String, Object> params = new HashMap<>();
	        params.put("type", type);
	        params.put("id", "work".equals(type) ? codeValueVO.getTaskClassificationId() : codeValueVO.getTaskPriorityId());
	        codeValueMapper.resetDefaultYn(params);
	    }
	    codeValueMapper.updateCodeValue(codeValueVO);
	}

	// 수정 (모든 기본값을 N으로 변경)
	@Override
	public void resetDefaultYn(Map<String, Object> params) {
	    codeValueMapper.resetDefaultYn(params);
	}

	// 추가 등록 시 모든 기본값을 N으로 변경
	@Override
	public void resetAllDefaultYn(String type) {
	    codeValueMapper.resetAllDefaultYn(type);
	}

	// 수정 (드래그 앤 드랍으로 순서 변경한 데이터 저장)
	//@Override
	//public void updateOrder(String type, String cno, long orderNum) {
	//	codeValueMapper.updateOrderNum(type, cno, orderNum);		
	//}

}
