package com.weple.cloud.project.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.weple.cloud.auth.service.LoginUserDetails;
import com.weple.cloud.project.service.ProjectService;
import com.weple.cloud.project.service.ProjectVO;

@Controller
public class ProjectController {

    private final ProjectService projectService;
    private static final String PERM_SELECT  = "k1_select";  // 모듈 설정 저장
    private static final String PERM_CREATE  = "k1_create";  // 프로젝트 생성/편집

    // 전체 모듈 목록
    // module_mapping 테이블의 module_name 값과 동일하게 맞춰야 함
    private static final List<Map<String, String>> ALL_MODULES;
    static {
        ALL_MODULES = new ArrayList<>();
        for (String[] m : new String[][]{
            {"b1",  "개요"},
            {"b2",  "작업내역"},
            {"b3",  "로드맵"},
            {"b4",  "일감"},
            {"b5",  "소요시간"},
            {"b6",  "간트차트"},
            {"b12", "테스트"},
            {"b7",  "위키"},
            {"b13", "파일관리"},
            {"b8",  "저장소"},
            {"b9",  "칸반보드"},
            {"b10", "캘린더"},
            {"b11", "설정"}
        }) {
            Map<String, String> map = new HashMap<>();
            map.put("name",  m[0]);
            map.put("label", m[1]);
            ALL_MODULES.add(map);
        }
    }
    
    private Set<String> findProjectPermissions(LoginUserDetails loginUser, Long projectId) {
        com.weple.cloud.auth.service.LoginUserVO user = loginUser.getLoginUser();
        if (isCompanyManager(user)) {
            return Set.of(PERM_SELECT, PERM_CREATE);
        }
        return projectService.findProjectPermissionCodes(user.getUserCode(), projectId);
    }

    private boolean hasPerm(Set<String> perms, String code) {
        return perms != null && perms.contains(code);
    }

    private boolean isCompanyManager(com.weple.cloud.auth.service.LoginUserVO user) {
        return Integer.valueOf(1).equals(user.getOwnerYn())
            || Integer.valueOf(1).equals(user.getAdminYn());
    }

    @Autowired
    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    // 프로젝트 목록 조회(페이징)
    @GetMapping("/project")
    public String projectList(
    		@AuthenticationPrincipal LoginUserDetails loginUser,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "1") int page,
            Model model) {

        int pageSize = 10;
        int offset = (page - 1) * pageSize;
        
        String userCode = loginUser.getLoginUser().getUserCode();
        boolean isManager = isCompanyManager(loginUser.getLoginUser());

        List<ProjectVO> list;
        int totalCount;

        if (isManager) {
            // 관리자는 소속 회사의 전체 프로젝트 조회 (구성원으로 등록 안 된 프로젝트도 포함)
            // - 구성원 추가 등 관리 작업을 위해 관리자는 프로젝트 진입이 가능해야 함
            String companyId = String.valueOf(loginUser.getLoginUser().getCompanyId());
            list       = projectService.findAllByCompany(companyId, keyword, offset, pageSize);
            totalCount = projectService.countAllByCompany(companyId, keyword);
        } else {
            // 일반 사용자는 본인이 속한 프로젝트만 조회
            list       = projectService.findAllByMember(userCode, keyword, offset, pageSize);
            totalCount = projectService.countAllByMember(userCode, keyword);
        }
        
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);
        
        // 관리자가 아니어도 k1_create 권한을 가진 역할을 맡고 있으면 생성 버튼 노출
        boolean canCreateProject = isManager
                || projectService.findAnyProjectPermissionCodes(userCode).contains("k1_create");

        model.addAttribute("projects", list);
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("canCreateProject", canCreateProject);
        model.addAttribute("sidebarMenu", "project");
        model.addAttribute("currentMenu", "none");
        return "weple/project/list";
    }

    // 설정 페이지 - 프로젝트 탭 조회
    @GetMapping("/project/{projectId}/setting")
    public String settingPage(
    		@AuthenticationPrincipal LoginUserDetails loginUser,
    		@PathVariable Long projectId,
    		Model model) {

        Set<String> perms = findProjectPermissions(loginUser, projectId);

        // 모듈 설정 권한(k1_select)이 없으면 페이지 자체를 보여주지 않음 (멤버십만으로는 부족 - 사용자는 프로젝트-설정이 보이면 안 됨)
        if (!hasPerm(perms, PERM_SELECT)) {
            return "weple/access-denide";
        }

        // 현재 is_active = 'Y'인 모듈 (체크 여부 기준. 프로젝트 생성 시 관리-프로젝트에서 선택한 값이
        // 최초에는 그대로 is_active='Y'로 들어가 있으므로, 이 값이 곧 "관리-프로젝트 생성 때 선택한 항목")
        List<String> activeModules = projectService.findActiveModuleNames(projectId);
        Set<String> activeSet = new HashSet<>(activeModules);

        // 전체 모듈을 다 표시. b1(개요)/b11(설정)은 항상 강제 포함이므로 화면에서 숨김
        List<Map<String, Object>> modules = new ArrayList<>();
        for (Map<String, String> m : ALL_MODULES) {
            String name = m.get("name");

            if ("b1".equals(name) || "b11".equals(name)) continue;

            Map<String, Object> item = new HashMap<>();
            item.put("name",    name);
            item.put("label",   m.get("label"));
            item.put("checked", activeSet.contains(name));
            modules.add(item);
        }

        ProjectVO setting = projectService.findSettingById(projectId);

        model.addAttribute("setting", setting);
        model.addAttribute("modules", modules);
        model.addAttribute("moduleNames", activeModules);
        model.addAttribute("projectId", projectId);
        model.addAttribute("project", setting);
        model.addAttribute("sidebarMenu", "project");
        model.addAttribute("currentMenu", "setting");
        model.addAttribute("activeTab", "project");
        model.addAttribute("settingMenu", "project");
        model.addAttribute("canSaveSetting", hasPerm(perms, PERM_SELECT));
        return "weple/project/setting";
    }

    // 설정 페이지 - 프로젝트 탭 저장
    @PostMapping("/project/{projectId}/setting")
    public String saveSetting(
    		@AuthenticationPrincipal LoginUserDetails loginUser, 
    		@PathVariable Long projectId,
            @ModelAttribute ProjectVO vo,
            RedirectAttributes ra) {
    	
    	Set<String> perms = findProjectPermissions(loginUser, projectId);
        if (!hasPerm(perms, PERM_SELECT)) return "weple/access-denide";
        
        vo.setProjectId(projectId);
        // b1(개요), b11(설정)은 항상 강제 포함
        List<String> modules = vo.getModuleNames();
        if (modules == null) modules = new ArrayList<>();
        if (!modules.contains("b1"))  modules.add("b1");
        if (!modules.contains("b11")) modules.add("b11");
        vo.setModuleNames(modules);

        // saveProjectSetting 내부에서 module_mapping에 없던 모듈은 새로 등록, 있던 모듈은 is_active만 갱신
        projectService.saveProjectSetting(vo);

        ra.addFlashAttribute("toastMessage", "설정이 저장되었습니다.");
        return "redirect:/project/" + projectId + "/setting";
    }
}