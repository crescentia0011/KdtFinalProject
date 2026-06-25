package com.weple.cloud.system.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
@AllArgsConstructor
public class PermissionVO {
	private String permissionCode;
    private String permissionTag;
    private String permissionName;
    private String tagLabel;
}
