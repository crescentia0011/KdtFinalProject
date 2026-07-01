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
	public List<CodeValueVO> findCodeValueAll(long companyId) {
		return codeValueMapper.selectCodeValueAll(companyId);
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
		//기본값(defaultYn)이 "Y"이면 기존 기본값을 모두 "N"으로 변경
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
		//사용여부가 "N"이면 기본값도 "N"으로 변경, 기본값이 "Y"이면 같은 종류의 다른 기본값을 "N"으로 변경
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

	// 수정 (수정 시 모든 기본값을 N으로 변경)
	@Override
	public void resetDefaultYn(Map<String, Object> params) {
	    codeValueMapper.resetDefaultYn(params);
	}

	// 추가 등록 시 모든 기본값을 N으로 변경
	@Override
	public void resetAllDefaultYn(String type) {
	    codeValueMapper.resetAllDefaultYn(type);
	}

	//기본값 이름을 넘겨줌
	@Override
	public String findDefaultNameByType(String type, String cno) {
		return codeValueMapper.findDefaultNameByType(type, cno);
	}

	//드래그앤드랍
	@Override
	@Transactional
	public void reorderCodes(String type, List<CodeValueVO> itemList) {
		codeValueMapper.updateCodeOrder(itemList);
		
	}

}