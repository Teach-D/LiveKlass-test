package com.LiveKlass.LiveKlass.entity;

import com.LiveKlass.LiveKlass.enums.OutboxStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_outbox",
        uniqueConstraints = @UniqueConstraint(columnNames = "notification_id"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationOutbox {

    public static final int MAX_RETRIES = 3;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long notificationId;

    @Enumerated(EnumType.STRING)
    private OutboxStatus status;

    private int retryCount;

    private LocalDateTime createdAt;

    private LocalDateTime lockedAt;

    private LocalDateTime publishedAt;

    public static NotificationOutbox create(Long notificationId) {
        NotificationOutbox outbox = new NotificationOutbox();
        outbox.notificationId = notificationId;
        outbox.status = OutboxStatus.PENDING;
        outbox.retryCount = 0;
        outbox.createdAt = LocalDateTime.now();
        return outbox;
    }

    public boolean isExhausted() {
        return retryCount >= MAX_RETRIES;
    }
}
