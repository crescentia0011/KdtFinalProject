package com.weple.cloud.notification.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.weple.cloud.auth.service.LoginUserDetails;
import com.weple.cloud.notification.service.AlarmVO;
import com.weple.cloud.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    private static final int RECENT_LIMIT = 5;

    // 알림 목록 페이지
    @GetMapping("/notification/list")
    public String list(
            @RequestParam(value = "status", defaultValue = "all") String status,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @AuthenticationPrincipal LoginUserDetails loginUser,
            Model model) {

        String userCode = loginUser.getLoginUser().getUserCode();

        int pageSize = 10;
        int offset = (page - 1) * pageSize;

        List<AlarmVO> alarmList = notificationService.findAlarmList(userCode, status, offset, pageSize);
        int totalCount = notificationService.countAlarmList(userCode, status);
        int totalPages = Math.max(1, (int) Math.ceil((double) totalCount / pageSize));
        int unreadCount = notificationService.countUnread(userCode);

        model.addAttribute("alarmList", alarmList);
        model.addAttribute("status", status);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("unreadCount", unreadCount);

        model.addAttribute("sidebarMenu", "none");
        model.addAttribute("currentMenu", "none");

        return "weple/notification/list";
    }

    // 헤더 드롭다운 - 최근 알림 N건 (fragment) - getCommentFragment와 동일한 패턴
    @GetMapping("/notification/popover")
    public String popover(@AuthenticationPrincipal LoginUserDetails loginUser, Model model) {
        String userCode = loginUser.getLoginUser().getUserCode();

        List<AlarmVO> items = notificationService.findRecentAlarmList(userCode, RECENT_LIMIT);
        int unreadCount = notificationService.countUnread(userCode);

        model.addAttribute("recentAlarmList", items);
        model.addAttribute("unreadCount", unreadCount);

        return "weple/notification/popover :: #popoverBody";
    }

    // 실시간 알림 팝업(토스트) 감지용 - 가장 최근 알림 1건 + 읽지 않은 개수 (폴링)
    @GetMapping("/notification/latest")
    @ResponseBody
    public ResponseEntity<?> latest(@AuthenticationPrincipal LoginUserDetails loginUser) {
        String userCode = loginUser.getLoginUser().getUserCode();

        List<AlarmVO> latest = notificationService.findRecentAlarmList(userCode, 1);
        int unreadCount = notificationService.countUnread(userCode);

        // 알림이 없을 수 있어(latest == null) HashMap 사용 - Map.of()는 null value를 허용하지 않음
        Map<String, Object> body = new HashMap<>();
        body.put("latest", latest.isEmpty() ? null : latest.get(0));
        body.put("unreadCount", unreadCount);

        return ResponseEntity.ok(body);
    }
    @GetMapping("/notification/unread-count")
    @ResponseBody
    public ResponseEntity<?> unreadCount(@AuthenticationPrincipal LoginUserDetails loginUser) {
        String userCode = loginUser.getLoginUser().getUserCode();
        return ResponseEntity.ok(Map.of("unreadCount", notificationService.countUnread(userCode)));
    }

    // 알림 1건 읽음/읽지 않음 토글 (AJAX)
    @PostMapping("/notification/{alarmId}/toggle")
    @ResponseBody
    public ResponseEntity<?> toggle(
            @PathVariable("alarmId") Long alarmId,
            @AuthenticationPrincipal LoginUserDetails loginUser) {

        String userCode = loginUser.getLoginUser().getUserCode();
        AlarmVO updated = notificationService.toggleCheck(alarmId, userCode);

        if (updated == null) {
            return ResponseEntity.notFound().build();
        }

        int unreadCount = notificationService.countUnread(userCode);
        return ResponseEntity.ok(Map.of(
                "checkYn", updated.getCheckYn(),
                "unreadCount", unreadCount
        ));
    }

    // 알림 모두 읽음 처리 (AJAX)
    @PostMapping("/notification/read-all")
    @ResponseBody
    public ResponseEntity<?> readAll(@AuthenticationPrincipal LoginUserDetails loginUser) {
        String userCode = loginUser.getLoginUser().getUserCode();
        notificationService.readAll(userCode);
        return ResponseEntity.ok(Map.of("unreadCount", 0));
    }

    // 알림 클릭 → 읽음 처리 후 연관 화면으로 이동
    @GetMapping("/notification/{alarmId}/go")
    public String go(
            @PathVariable("alarmId") Long alarmId,
            @AuthenticationPrincipal LoginUserDetails loginUser) {

        String userCode = loginUser.getLoginUser().getUserCode();

        AlarmVO alarm = notificationService.findById(alarmId, userCode);
        if (alarm == null) {
            return "redirect:/notification/list";
        }

        notificationService.markRead(alarmId, userCode);

        String url = alarm.getRedirectUrl();
        return "redirect:" + (url != null && !url.isBlank() ? url : "/notification/list");
    }
}