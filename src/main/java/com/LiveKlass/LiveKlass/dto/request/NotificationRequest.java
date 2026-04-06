package com.LiveKlass.LiveKlass.dto.request;

import com.LiveKlass.LiveKlass.enums.NotificationChannel;
import com.LiveKlass.LiveKlass.enums.NotificationType;
import com.LiveKlass.LiveKlass.enums.SendTimeSlot;
import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class NotificationRequest {

    private Long userId;
    private NotificationType type;
    private String eventId;
    private List<NotificationChannel> channels;
    private SendTimeSlot sendTimeSlot;
}