package com.weple.cloud.system.service;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class SystemModuleVO {
	private String commonId;
    private String commonCode;
    private String defaultDescribe;
}
