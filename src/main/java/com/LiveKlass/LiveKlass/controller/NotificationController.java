package com.LiveKlass.LiveKlass.controller;

import com.LiveKlass.LiveKlass.dto.request.NotificationRequest;
import com.LiveKlass.LiveKlass.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<String> createNotification(@RequestBody NotificationRequest request) {
        notificationService.registerNotification(request);
        return ResponseEntity.ok("알림 요청이 정상적으로 접수되었습니다.");
    }
}