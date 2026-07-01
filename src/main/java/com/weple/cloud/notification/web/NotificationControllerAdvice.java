package com.weple.cloud.notification.web;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.weple.cloud.auth.service.LoginUserDetails;
import com.weple.cloud.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;

/**
 * 모든 페이지(Thymeleaf 뷰를 렌더링하는 컨트롤러)에
 * 읽지 않은 알림 개수(headerUnreadCount)를 자동으로 model에 공급한다.
 *
 * header.html 의 notifUnreadBadge 가 페이지 로드 순간부터 바로 숫자를 보여줄 수 있도록,
 * JS 폴링 결과를 기다리지 않고 서버 렌더링 시점에 이미 값을 갖고 있게 한다.
 *
 * annotations = {} : @RestController(@ResponseBody)가 붙은 API 컨트롤러는 제외.
 * Thymeleaf 뷰를 반환하는 @Controller에서만 실행되므로 REST API 호출 시 불필요한 DB 조회가 없다.
 */
@ControllerAdvice(annotations = org.springframework.stereotype.Controller.class)
@RequiredArgsConstructor
public class NotificationControllerAdvice {

    private final NotificationService notificationService;

    @ModelAttribute("headerUnreadCount")
    public int headerUnreadCount(@AuthenticationPrincipal LoginUserDetails loginUser) {
        if (loginUser == null) {
            return 0; // 비로그인(로그인 화면 등)에서는 0
        }
        return notificationService.countUnread(loginUser.getLoginUser().getUserCode());
    }
}