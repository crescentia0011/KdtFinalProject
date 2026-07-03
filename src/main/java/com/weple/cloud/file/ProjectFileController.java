package com.weple.cloud.file;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import com.weple.cloud.auth.service.LoginUserDetails;
import com.weple.cloud.auth.service.LoginUserVO;
import com.weple.cloud.project.service.ProjectService;
import com.weple.cloud.project.service.ProjectVO;
import com.weple.cloud.task.service.TaskService;
import com.weple.cloud.task.service.TaskVO;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

@Controller
@RequiredArgsConstructor
@RequestMapping("/project/{projectId}/file")
public class ProjectFileController {
	
	private final ProjectFileService projectFileService;
	private final ProjectService projectService;
	private final S3Service s3Service;

	private boolean isCompanyManager(com.weple.cloud.auth.service.LoginUserVO user) {
		return Integer.valueOf(1).equals(user.getOwnerYn())
			|| Integer.valueOf(1).equals(user.getAdminYn());
	}

	private boolean canAccess(LoginUserDetails loginUser, String projectId) {
		try {
			if (isCompanyManager(loginUser.getLoginUser())) return true;
			return projectService.isMember(loginUser.getLoginUser().getUserCode(), Long.valueOf(projectId));
		} catch (Exception e) {
			return false;
		}
	}

	// -------------------------------파일관리------------------------------		
	// 전체조회
	@GetMapping({"", "/projectFileList"})
	public String projectFileList(@PathVariable String projectId, Model model,
			@AuthenticationPrincipal LoginUserDetails loginUser) {
	    if (!canAccess(loginUser, projectId)) return "weple/access-denide";
	    List<ProjectFileVO> list = projectFileService.findProjectFileAll(projectId);
	    ProjectVO project = projectService.findById(projectId);
	    model.addAttribute("projectFileList", list);
	    model.addAttribute("projectId", projectId);
	    model.addAttribute("project", project);
	    model.addAttribute("isManager", isCompanyManager(loginUser.getLoginUser()));
	    model.addAttribute("currentMenu", "file");
	    model.addAttribute("sidebarMenu", "project");
	    return "weple/file/list";
	}
	
	// 상세조회
	@GetMapping("/projectFileInfo")
	public String projectFileInfo(@PathVariable String projectId, String fileId, Model model,
			@AuthenticationPrincipal LoginUserDetails loginUser) {
		if (!canAccess(loginUser, projectId)) return "weple/access-denide";
		ProjectFileVO projectFileInfoVO = projectFileService.findProjectFileInfo(fileId);
		List<ProjectFileVersionsVO> versionList = projectFileService.findProjectFileVersionAll(fileId);
	    
	    System.out.println(">>>>>> fileId: " + fileId);              // fileId가 제대로 오는지
	    System.out.println(">>>>>> 버전 사이즈: " + versionList.size()); // 0이면 DB/매퍼 문제
		ProjectVO project = projectService.findById(projectId);
		model.addAttribute("projectFileInfo", projectFileInfoVO);
		model.addAttribute("projectFileVersionList", versionList);
		model.addAttribute("projectId", projectId);
		model.addAttribute("project", project);
		model.addAttribute("isManager", isCompanyManager(loginUser.getLoginUser()));
		
		model.addAttribute("currentMenu", "file");
		model.addAttribute("sidebarMenu", "project");
		return "weple/file/detail";
	}
		
	// 등록
	@GetMapping("/projectFileInsert")
	public String projectFileInsertForm(@PathVariable String projectId,
			@AuthenticationPrincipal LoginUserDetails loginUser) {
		if (!canAccess(loginUser, projectId)) return "weple/access-denide";
		return "weple/file/list";
	}
	
	@PostMapping("/projectFileInsert")
	public String projectFileInsertProcess(@PathVariable String projectId, ProjectFileVO projectFileVO,
			@AuthenticationPrincipal LoginUserDetails loginUser) {
		if (!canAccess(loginUser, projectId)) return "weple/access-denide";
		String fno = projectFileService.addProjectFile(projectFileVO);
		return "redirect:projectFileInfo?fno=" + fno;
	}
		
	// 삭제 (관리자만 가능)
	@GetMapping("/deleteProjectFile")
	public String deleteProjectFile(@PathVariable String projectId, String fileId,
			@AuthenticationPrincipal LoginUserDetails loginUser) {
		if (!isCompanyManager(loginUser.getLoginUser())) return "weple/access-denide";
		try {
			// file_download_history가 file_versions를 참조하고 있어서, 버전을 지우기 전에
			// 그 버전들에 걸린 다운로드 이력부터 먼저 지워야 FK 제약에 안 걸림
			projectFileService.removeDownloadHistoryByFileId(fileId);
			projectFileService.removeProjectFileVersionByFileId(fileId);
			projectFileService.removeProjectFile(fileId);
		} catch (Exception e) {
			e.printStackTrace();
			return "redirect:/project/" + projectId + "/file?error=delete_failed";
		}
		return "redirect:/project/" + projectId + "/file?deleted=1";
	}
	
	// -------------------------------파일 버전------------------------------
    @GetMapping("/projectFileVersionList")
    public String projectFileVersionList(String fileId, Model model) {
    	List<ProjectFileVersionsVO> list = projectFileService.findProjectFileVersionAll(fileId);
    	model.addAttribute("projectFileVersionList", list);
    	return "weple/file/list";
    }
    
    // 상세조회
    @GetMapping("/projectFileVersionInfo")
    public String projectFileVersionInfo(@PathVariable String projectId, String versionId, Model model) {
        ProjectFileVersionsVO vo = projectFileService.findProjectFileVersionInfo(versionId);
        model.addAttribute("projectFileVersionInfo", vo);
        return "weple/file/versionDetail";
    }
    
    // 등록
    @GetMapping("/projectFileVersionInsert")
	public String projectFileVersionForm() {
		return "weple/file/list";
	}    
    
    @PostMapping("/projectFileVersionInsert")
    public String projectFileVersionProcess(ProjectFileVersionsVO projectFileVersionsVO) {
    	String fvno = projectFileService.addProjectFileVersion(projectFileVersionsVO);
    	return "redirect:projectFileVersionInfo?fvno=" + fvno;
    }
    
    // 삭제
    @GetMapping("/projectFileVersionDelete")
    public String projectFileVersionDelete(@PathVariable String projectId, String versionId) {
    	long result = projectFileService.removeProjectFileVersion(versionId);
    	return "redirect:projectFileVersionList?versionId=" + versionId;
    }
    
    // 파일 업로드
    @PostMapping("/projectFileUpload")
    public String projectFileUpload(@PathVariable String projectId,
                                     @RequestParam("file") MultipartFile file,
                                     @RequestParam(value = "taskId", required = false) String taskId,
                                     @AuthenticationPrincipal LoginUserDetails loginUser) {

        if (!canAccess(loginUser, projectId)) return "weple/access-denide";

        String originalName = file.getOriginalFilename();
        String savedName = UUID.randomUUID().toString() + "_" + originalName;

        try {
            s3Service.uploadFile(file, savedName);   // 로컬 File/transferTo 대신 S3로 업로드
        } catch (IOException e) {
            e.printStackTrace();
            return "redirect:/project/" + projectId + "/file?error=upload_failed";
        }

        LoginUserVO user = loginUser.getLoginUser();
        String uploaderCode = user.getUserCode();

        Long pId = Long.valueOf(projectId);

        // 같은 프로젝트 + 같은 일감(또는 둘 다 일감 미연결) 안에 동일한 파일명이 이미 있으면
        // 새 파일을 만들지 않고 기존 파일에 새 버전만 추가한다.
        // 일감이 다르면(혹은 한쪽만 일감에 연결돼 있으면) 파일명이 같아도 별도 파일로 취급한다.
        String fileId = projectFileService.findProjectFileIdByName(pId, taskId, originalName);
        long nextVersionNumber;

        if (fileId == null) {
            ProjectFileVO fileVO = new ProjectFileVO();
            fileVO.setTaskId(taskId);
            fileVO.setLogicalName(originalName);
            fileVO.setIsDeleted("N");
            fileVO.setProjectId(pId);
            // createdAt은 매퍼에서 SYSDATE로 채우니 안 넣어도 됨

            fileId = projectFileService.addProjectFile(fileVO);

            if ("-1".equals(fileId)) {
                return "redirect:/project/" + projectId + "/file?error=insert_failed";
            }
            nextVersionNumber = 1;
        } else {
            Long maxVersion = projectFileService.findMaxVersionNumber(fileId);
            nextVersionNumber = (maxVersion == null ? 0 : maxVersion) + 1;
        }

        ProjectFileVersionsVO versionVO = new ProjectFileVersionsVO();
        versionVO.setFileId(fileId);
        versionVO.setVersionNumber(nextVersionNumber);
        versionVO.setSavedName(savedName);       // S3 키 그대로 저장 (로컬 경로 X)
        versionVO.setFilePath(null);             // 더 이상 로컬 경로 안 씀, null로 두거나 컬럼 자체를 비워도 됨
        versionVO.setFileSize(file.getSize());
        versionVO.setUploader(uploaderCode);
        // uploadedAt도 매퍼에서 SYSDATE로 채움

        projectFileService.addProjectFileVersion(versionVO);

        return "redirect:/project/" + projectId + "/file/projectFileInfo?fileId=" + fileId;
    }
    
    
    private final TaskService taskService;
    // 일감 검색용
    @GetMapping("/taskSearchList")
    @ResponseBody
    public List<Map<String, Object>> taskSearchList(@PathVariable String projectId) {
        Map<String, Object> filterParams = new HashMap<>();
        filterParams.put("projectId", Long.valueOf(projectId));
        filterParams.put("tManager", null);
        filterParams.put("searchKeyword", null);
        filterParams.put("typeIds", null);
        filterParams.put("statusIds", null);
        filterParams.put("priorityNames", null);
        filterParams.put("memberIds", null);
        filterParams.put("progress", null);
        filterParams.put("regDate", null);
        filterParams.put("dueDate", null);
        filterParams.put("offset", 0);
        filterParams.put("limit", 1000); // 충분히 크게

        List<TaskVO> tasks = taskService.findAllWithFilters(filterParams);

        List<Map<String, Object>> result = new ArrayList<>();
        for (TaskVO t : tasks) {
            Map<String, Object> m = new HashMap<>();
            m.put("taskId", t.getTaskId());
            m.put("taskTitle", t.getTaskTitle());
            result.add(m);
        }
        return result;
    }
    
    // 다운로드
    @GetMapping("/projectFileDownload/{versionId}")
    public ResponseEntity<Resource> projectFileDownload(@PathVariable String projectId,
                                                         @PathVariable String versionId,
                                                         @AuthenticationPrincipal LoginUserDetails loginUser) {
        if (!canAccess(loginUser, projectId)) {
            return ResponseEntity.status(403).build();
        }
        ProjectFileVersionsVO versionInfo = projectFileService.findVersionForDownload(versionId);

        if (versionInfo == null || versionInfo.getSavedName() == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            ResponseInputStream<GetObjectResponse> s3Object = s3Service.downloadFile(versionInfo.getSavedName());
            Resource resource = new InputStreamResource(s3Object);

            String downloaderCode = loginUser.getLoginUser().getUserCode();
            projectFileService.recordDownloadHistory(versionId, downloaderCode);

            String encodedFileName = UriUtils.encode(versionInfo.getLogicalName(), StandardCharsets.UTF_8);
            String contentDisposition = "attachment; filename=\"" + encodedFileName + "\"";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                    .header(HttpHeaders.CONTENT_TYPE, "application/octet-stream")
                    .body(resource);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // 다운로드 이력 화면 (관리자만 가능)
    @GetMapping("/downloader")
    public String downloader(@PathVariable String projectId, Model model,
            @AuthenticationPrincipal LoginUserDetails loginUser) {
        if (!isCompanyManager(loginUser.getLoginUser())) return "weple/access-denide";
        ProjectVO project = projectService.findById(projectId);
        model.addAttribute("projectId", projectId);
        model.addAttribute("project", project);
        model.addAttribute("currentMenu", "file");
        model.addAttribute("sidebarMenu", "project");
        model.addAttribute("downloadHistoryList", projectFileService.findDownloadHistory(projectId));
        return "weple/file/downloader";
    }
    
}