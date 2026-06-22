package com.weple.cloud.project.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.weple.cloud.history.worklog.service.WorkLogVO;

@Mapper
public interface ProjectWorkLogMapper {
	List<WorkLogVO> selectProjectWorkLog(
			@Param("projectId") String projectId,
			@Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("userCode") String userCode,
            @Param("typeNames") List<String> typeNames);
}
