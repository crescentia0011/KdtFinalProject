package com.weple.cloud.project.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.weple.cloud.project.mapper.ProjectMemberMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProjectMemberServiceImpl implements ProjectMemberService {

    private final ProjectMemberMapper memberMapper;

    @Override
    public List<ProjectMemberVO> findMemberList(Long projectId) {
        return memberMapper.selectMemberList(projectId);
    }

    @Override
    public List<ProjectMemberVO> searchUsersForAdd(Long projectId, String keyword) {
        ProjectMemberSearchVO searchVO = new ProjectMemberSearchVO();
        searchVO.setProjectId(projectId);
        searchVO.setKeyword(keyword);
        return memberMapper.searchUsersForAdd(searchVO);
    }

    @Override
    public List<ProjectMemberRoleVO> findRoleList() {
        return memberMapper.selectRoleList();
    }

    @Override
    @Transactional
    public int addMember(ProjectMemberVO vo) {
        int result = memberMapper.insertMember(vo);
        if (result > 0 && vo.getRoleId() != null) {
            memberMapper.insertMemberRole(vo.getMemberId(), vo.getRoleId());
        }
        return result;
    }

    @Override
    @Transactional
    public int removeMember(Long memberId, Long projectId) {
        memberMapper.deleteMemberRoles(memberId);
        return memberMapper.deleteMember(memberId, projectId);
    }

	@Override
	public List<ProjectMemberVO> findGroupList() {
		return memberMapper.selectGroupList();
	}

	@Override
	public List<ProjectMemberVO> findUsersByGroupId(Long groupId, Long projectId) {
		return memberMapper.selectUsersByGroupId(groupId, projectId);
	}
}