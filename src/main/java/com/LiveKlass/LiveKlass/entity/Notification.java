package com.LiveKlass.LiveKlass.entity;

import com.LiveKlass.LiveKlass.enums.NotificationChannel;
import com.LiveKlass.LiveKlass.enums.NotificationStatus;
import com.LiveKlass.LiveKlass.enums.NotificationType;
import com.LiveKlass.LiveKlass.enums.SendTimeSlot;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Notification extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String eventId;
    private Long userId;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    private NotificationStatus status;

    @Enumerated(EnumType.STRING)
    private NotificationChannel channel;

    @Enumerated(EnumType.STRING)
    private SendTimeSlot sendTimeSlot;

    private boolean isRead = false;

    private String failureReason;

    @Builder
    public Notification(String eventId, Long userId, NotificationType type, NotificationStatus status, NotificationChannel channel, SendTimeSlot sendTimeSlot, boolean isRead) {
        this.eventId = eventId;
        this.userId = userId;
        this.type = type;
        this.status = status;
        this.channel = channel;
        this.sendTimeSlot = sendTimeSlot;
        this.isRead = isRead;
    }

    public void markAsRead() {
        this.isRead = true;
    }

    public void updateStatus(NotificationStatus status) {
        this.status = status;
    }

}