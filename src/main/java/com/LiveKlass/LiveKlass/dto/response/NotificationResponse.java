package com.LiveKlass.LiveKlass.dto.response;

import com.LiveKlass.LiveKlass.entity.Notification;
import com.LiveKlass.LiveKlass.enums.NotificationChannel;
import com.LiveKlass.LiveKlass.enums.NotificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class NotificationResponse {

    private Long id;
    private String eventId;
    private NotificationStatus status;
    private NotificationChannel channel;
    private LocalDateTime createdAt;
    private boolean isRead;
    private String failureReason;

    public static NotificationResponse convertToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .eventId(notification.getEventId())
                .status(notification.getStatus())
                .channel(notification.getChannel())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .failureReason(notification.getFailureReason())
                .build();
    }
}