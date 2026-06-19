package com.weple.cloud.history.worklog.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.weple.cloud.history.worklog.service.WorkLogVO;

@Mapper
public interface WorkLogMapper {
	public List<WorkLogVO> selectAll();
}
