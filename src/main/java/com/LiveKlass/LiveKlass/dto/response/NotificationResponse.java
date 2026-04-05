package com.LiveKlass.LiveKlass.dto.response;

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
}