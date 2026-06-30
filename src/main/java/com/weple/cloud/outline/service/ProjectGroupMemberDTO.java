package com.weple.cloud.outline.service;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ProjectGroupMemberDTO {
    private String groupName;    // 그룹명
    private int memberCount;     // 그룹 내 참여 인원수 (n명)
    private String memberNames;  // 멤버 이름들 (ooo, ooo, ooo)

    
    // 포맷팅된 문자열을 반환하는 메서드
    public String getDisplayFormat() {
        String group = (this.groupName != null) ? this.groupName : "그룹 없음";
        return group + " : " + this.memberCount + "명, (" + this.memberNames + ")";
    }
}
