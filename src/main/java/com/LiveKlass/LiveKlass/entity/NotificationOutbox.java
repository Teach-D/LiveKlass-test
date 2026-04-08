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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long notificationId;

    @Enumerated(EnumType.STRING)
    private OutboxStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime publishedAt;

    public static NotificationOutbox create(Long notificationId) {
        NotificationOutbox outbox = new NotificationOutbox();
        outbox.notificationId = notificationId;
        outbox.status = OutboxStatus.PENDING;
        outbox.createdAt = LocalDateTime.now();
        return outbox;
    }

    public void markAsPublished() {
        this.status = OutboxStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
    }
}
