package com.LiveKlass.LiveKlass.controller;

import com.LiveKlass.LiveKlass.dto.request.NotificationRequest;
import com.LiveKlass.LiveKlass.dto.response.NotificationResponse;
import com.LiveKlass.LiveKlass.dto.response.NotificationStatusResponse;
import com.LiveKlass.LiveKlass.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getNotification(@PathVariable Long id) {
        NotificationResponse response = notificationService.getNotification(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getUserNotifications(@RequestParam Long userId, @RequestParam(required = false) Boolean isRead) {
        List<NotificationResponse> responses = notificationService.getUserNotifications(userId, isRead);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<NotificationStatusResponse> getNotificationStatusOnly(@PathVariable Long id) {
        NotificationStatusResponse response = notificationService.getOnlyStatus(id);
        return ResponseEntity.ok(response);
    }
}