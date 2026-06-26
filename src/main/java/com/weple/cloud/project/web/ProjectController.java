package com.weple.cloud.project.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.weple.cloud.project.service.ProjectService;
import com.weple.cloud.project.service.ProjectVO;

@Controller
public class ProjectController {
	
	private final ProjectService projectService;
	
	// 전체 모듈 목록
	// module_mapping 테이블의 module_name 값과 동일하게 맞춰야 함
	private static final List<Map<String, String>> ALL_MODULES;
	static {
        ALL_MODULES = new ArrayList<>();
        for (String[] m : new String[][]{
        	{"개요", "개요"},
            {"작업내역", "작업내역"},
            {"마일스톤", "마일스톤"},
            {"일감", "일감"},
            {"소요시간", "소요시간"},
            {"간트차트", "간트차트"},
            {"테스트", "테스트"},
            {"위키", "위키"},
            {"파일관리", "파일관리"},
            {"저장소", "저장소"},
            {"칸반보드", "칸반보드"},
            {"캘린더", "캘린더"}
        }) {
            Map<String, String> map = new HashMap<>();
            map.put("name",  m[0]); // DB 저장값
            map.put("label", m[1]); // 화면 표시값
            ALL_MODULES.add(map);
        }
    }
	
	@Autowired
	public ProjectController(ProjectService projectService) {
		this.projectService = projectService;
	}
	
	// 프로젝트 목록 조회(페이징)
	@GetMapping("/project")
	public String projectList(
			@RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "page", defaultValue = "1") int page,
			Model model) {
		
		int pageSize = 10;
		int offset = (page-1) * pageSize;
		
		List<ProjectVO> list = projectService.findAll(keyword, offset, pageSize);
		int totalCount = projectService.countAll(keyword);
		int totalPages = (int) Math.ceil((double) totalCount / pageSize);
		
		model.addAttribute("projects", list);
		model.addAttribute("keyword", keyword);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", totalPages);
		
		model.addAttribute("sidebarMenu", "project");
		model.addAttribute("currentMenu", "none");
		return "weple/project/list";
	}
	
	// 설정 페이지 - 프로젝트 탭 조회
	@GetMapping("/project/{projectId}/setting")
	public String settingPage(@PathVariable Long projectId, Model model) {
		// 프로젝트 설정 정보+활성 모듈명 조회
		ProjectVO setting = projectService.findSettingById(projectId);
		Set<String> enabledSet = new HashSet<>(setting.getModuleNames());
		
		// 전체 모듈에 체크 여부 추가
		List<Map<String, Object>> modules =new ArrayList<>();
		for (Map<String, String> m : ALL_MODULES){
			Map<String, Object> item = new HashMap<>();
			item.put("name", m.get("name"));
			item.put("label", m.get("label"));
			item.put("checked", enabledSet.contains(m.get("name")));
			modules.add(item);
		}
		
		model.addAttribute("setting", setting);
		model.addAttribute("modules", modules);
		
		model.addAttribute("moduleNames", setting.getModuleNames());
		model.addAttribute("projectId", projectId);
		model.addAttribute("project", setting);
		model.addAttribute("sidebarMenu", "project");
		model.addAttribute("activeTab", "project");
		model.addAttribute("settingMenu", "project");
		return "weple/project/setting";
		
	}
	
	
	// 설정 페이지 - 프로젝트 탭 저장
	@PostMapping("/project/{projectId}/setting")
	public String saveSetting(@PathVariable Long projectId,
							  @ModelAttribute ProjectVO vo,
							  RedirectAttributes ra) {
		// PathVariable 로 받은 projectId 를 VO 에 세팅
		vo.setProjectId(projectId);
		projectService.saveProjectSetting(vo);
		
		ra.addFlashAttribute("toastMessage", "설정이 저장되었습니다.");
		return "redirect:/project/" + projectId + "/setting";
	}
}
