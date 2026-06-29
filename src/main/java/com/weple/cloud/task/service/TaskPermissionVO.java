package com.weple.cloud.task.service;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class TaskPermissionVO {
	private String k3Add;
    private String k3Edit;
    private String k3Delete;
    private String k3Myedit;
    private String k3Comment;
}
