package com.weple.cloud.project.web;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.weple.cloud.notification.service.AlarmType;
import com.weple.cloud.notification.service.NotificationService;
import com.weple.cloud.project.service.ProjectMemberRoleVO;
import com.weple.cloud.project.service.ProjectMemberService;
import com.weple.cloud.project.service.ProjectMemberVO;
import com.weple.cloud.project.service.ProjectService;
import com.weple.cloud.project.service.ProjectVO;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ProjectMemberController {

    private final ProjectMemberService memberService;
    private final ProjectService projectService;
    private final NotificationService notificationService;

    //  설정 > 구성원 탭
    @GetMapping("/project/settings/members")
    public String membersPage(
            @RequestParam Long projectId,
            Model model) {

        List<ProjectMemberVO> memberList = memberService.findMemberList(projectId);
        List<ProjectMemberRoleVO> roleList   = memberService.findRoleList();
        List<ProjectMemberVO> groupList  = memberService.findGroupList();

        model.addAttribute("project", projectService.findById(String.valueOf(projectId)));
        model.addAttribute("moduleNames", projectService.findActiveModuleNames(projectId));
        model.addAttribute("sidebarMenu", "project");
        model.addAttribute("currentMenu", "setting");
        model.addAttribute("projectId", projectId);
        model.addAttribute("memberList", memberList);
        model.addAttribute("roleList", roleList);
        model.addAttribute("groupList", groupList);
        model.addAttribute("activeTab", "member");
        model.addAttribute("settingMenu", "member");

        return "weple/project/members";
    }

    //  구성원 추가 모달 - 사용자 검색 AJAX
    @GetMapping("/project/settings/members/search")
    @ResponseBody
    public ResponseEntity<List<ProjectMemberVO>> searchUsers(
            @RequestParam Long projectId,
            @RequestParam(required = false, defaultValue = "") String keyword) {

        List<ProjectMemberVO> result = memberService.searchUsersForAdd(projectId, keyword);
        return ResponseEntity.ok(result);
    }

    //  그룹별 사용자 조회
    @GetMapping("/project/settings/members/group")
    @ResponseBody
    public ResponseEntity<List<ProjectMemberVO>> getUsersByGroup(
            @RequestParam Long groupId,
            @RequestParam Long projectId) {
        return ResponseEntity.ok(memberService.findUsersByGroupId(groupId, projectId));
    }

    //  구성원 추가
    @PostMapping("/project/settings/members/add")
    @ResponseBody
    public ResponseEntity<String> addMember(
            @RequestParam Long projectId,
            @RequestParam String userCode,
            @RequestParam Long roleId) {

        ProjectMemberVO vo = new ProjectMemberVO();
        vo.setProjectId(projectId);
        vo.setUserCode(userCode);
        vo.setRoleId(roleId);

        try {
            memberService.addMember(vo);
            // 알림-은지(프로젝트 초대)
            ProjectVO project = projectService.findById(String.valueOf(projectId));
            String projectTitle = (project != null) ? project.getProjectTitle() : "프로젝트";
            
            notificationService.create(
                    userCode,
                    AlarmType.TAG_PROJECT_INVITE,
                    "\"" + projectTitle + "\" 프로젝트에 참여자로 등록되었습니다.",
                    AlarmType.TARGET_PROJECT,
                    String.valueOf(projectId)
                );
            
            return ResponseEntity.ok("ok");
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    //  구성원 삭제
    @PostMapping("/project/settings/members/delete")
    @ResponseBody
    public ResponseEntity<String> deleteMember(
            @RequestParam Long memberId,
            @RequestParam Long projectId) {

        try {
            memberService.removeMember(memberId, projectId);
            return ResponseEntity.ok("ok");
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }
}