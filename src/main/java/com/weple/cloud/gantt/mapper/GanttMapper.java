package com.weple.cloud.gantt.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.weple.cloud.task.service.TaskVO;

public interface GanttMapper {
	List<TaskVO> selectTaskAll(@Param("projectId") Long projectId);
}
