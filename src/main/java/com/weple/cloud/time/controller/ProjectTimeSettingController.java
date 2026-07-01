package com.weple.cloud.time.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.weple.cloud.auth.service.LoginUserDetails;
import com.weple.cloud.project.service.ProjectService;
import com.weple.cloud.time.service.ProjectTimeSettingService;
import com.weple.cloud.system.service.CodeValueVO;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ProjectTimeSettingController {

    private final ProjectTimeSettingService timeSettingService;
    private final ProjectService projectService;

    // 설정 > 시간추적 탭
    @GetMapping("/project/settings/time")
    public String timeSettingPage(@AuthenticationPrincipal LoginUserDetails loginUser,
                                   @RequestParam Long projectId,
                                   Model model) {
        Long companyId = loginUser.getLoginUser().getCompanyId();

        List<CodeValueVO> classificationList = timeSettingService.findClassificationOptions(companyId);
        List<String> selectedIds = timeSettingService.findSelectedClassificationIds(projectId);

        model.addAttribute("project", projectService.findById(String.valueOf(projectId)));
        model.addAttribute("moduleNames", projectService.findActiveModuleNames(projectId));
        model.addAttribute("sidebarMenu", "project");
        model.addAttribute("currentMenu", "setting");
        model.addAttribute("projectId", projectId);
        model.addAttribute("classificationList", classificationList);
        model.addAttribute("selectedIds", selectedIds);
        model.addAttribute("activeTab", "timeTracking");
        model.addAttribute("settingMenu", "timeTracking");

        return "weple/time/time-setting";
    }

    // 설정 > 시간추적 탭 저장
    @PostMapping("/project/settings/time")
    public String saveTimeSetting(@RequestParam Long projectId,
                                   @RequestParam(value = "enabledClassifications", required = false) List<String> enabledClassifications,
                                   RedirectAttributes redirectAttributes) {
        // 최소 1개 이상 사용 선택되어 있어야 저장 가능함
        if (enabledClassifications == null || enabledClassifications.isEmpty()) {
            redirectAttributes.addFlashAttribute("toastType", "error");
            redirectAttributes.addFlashAttribute("toastMessage", "작업분류를 최소 1개 이상 선택해주세요.");
            return "redirect:/project/settings/time?projectId=" + projectId;
        }

        timeSettingService.saveSelectedClassifications(projectId, enabledClassifications);
        redirectAttributes.addFlashAttribute("toastType", "success");
        redirectAttributes.addFlashAttribute("toastMessage", "시간추적 설정이 저장되었습니다.");
        return "redirect:/project/settings/time?projectId=" + projectId;
    }
}