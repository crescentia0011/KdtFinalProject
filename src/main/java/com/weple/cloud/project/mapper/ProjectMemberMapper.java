package com.weple.cloud.project.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.weple.cloud.project.service.ProjectMemberVO;
import com.weple.cloud.project.service.ProjectMemberSearchVO;
import com.weple.cloud.project.service.ProjectMemberRoleVO;

@Mapper
public interface ProjectMemberMapper {

    // 구성원 목록 조회
	public List<ProjectMemberVO> selectMemberList(@Param("projectId") Long projectId);

    // 구성원 추가용 사용자 검색(키워드: 이름 포함)
    // 이미 프로젝트에 소속된 사용자는 제외
	public List<ProjectMemberVO> searchUsersForAdd(ProjectMemberSearchVO searchVO);
	
	// 그룹 목록 조회
	public List<ProjectMemberVO> selectGroupList();
	
	// 그룹 내 사용자 조회(프로젝트 미소속 제외)
	public List<ProjectMemberVO> selectUsersByGroupId(@Param("groupId") Long groupId,
													  @Param("projectId") Long projectId);

    // 역할 목록 조회
	public List<ProjectMemberRoleVO> selectRoleList();

    // 구성원 추가
	public int insertMember(ProjectMemberVO vo);

    // 구성원 역할 추가(member_roles)
	public int insertMemberRole(@Param("memberId") Long memberId,
								@Param("roleId") Long roleId);

    // 구성원 삭제
	public int deleteMember(@Param("memberId") Long memberId,
                     		@Param("projectId") Long projectId);

    // 구성원 역할 삭제
	public int deleteMemberRoles(@Param("memberId") Long memberId);

    // memberId로 단건 조회
	public ProjectMemberVO selectMemberById(@Param("memberId") Long memberId);
}