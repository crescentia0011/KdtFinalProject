package com.weple.cloud.project.service;

import java.util.List;

public interface ProjectMemberService {

	public List<ProjectMemberVO> findMemberList(Long projectId);

	public List<ProjectMemberVO> searchUsersForAdd(Long projectId, String keyword);
	
	public List<ProjectMemberVO> findGroupList();
	 
	public List<ProjectMemberVO> findUsersByGroupId(Long groupId, Long projectId);

	public List<ProjectMemberRoleVO> findRoleList();

	public int addMember(ProjectMemberVO vo);

	public int removeMember(Long memberId, Long projectId);
}