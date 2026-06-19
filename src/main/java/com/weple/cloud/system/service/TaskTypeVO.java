package com.weple.cloud.system.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
public class TaskTypeVO {
	private Integer typeId;
	private Integer companyId;
	private String  typeName;
	private String  taskTypeDescribe;
	private Integer position;
}
