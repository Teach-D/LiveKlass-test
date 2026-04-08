package com.LiveKlass.LiveKlass.repository;

import com.LiveKlass.LiveKlass.entity.NotificationOutbox;
import com.LiveKlass.LiveKlass.enums.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface NotificationOutboxRepository extends JpaRepository<NotificationOutbox, Long> {

    Optional<NotificationOutbox> findByNotificationId(Long notificationId);

    @Query(value = "SELECT * FROM notification_outbox WHERE status = 'PENDING' FOR UPDATE SKIP LOCKED",
            nativeQuery = true)
    List<NotificationOutbox> findPendingForUpdate();

    @Modifying
    @Query("UPDATE NotificationOutbox o SET o.status = :status WHERE o.id IN :ids")
    void updateStatus(@Param("ids") List<Long> ids, @Param("status") OutboxStatus status);

    @Modifying
    @Query("UPDATE NotificationOutbox o SET o.status = 'LOCKED', o.lockedAt = :lockedAt WHERE o.id IN :ids")
    void lockWithTimestamp(@Param("ids") List<Long> ids, @Param("lockedAt") LocalDateTime lockedAt);

    @Query("SELECT o FROM NotificationOutbox o WHERE o.status = 'LOCKED' AND o.lockedAt < :threshold")
    List<NotificationOutbox> findStuckLocked(@Param("threshold") LocalDateTime threshold);

    @Modifying
    @Query("UPDATE NotificationOutbox o SET o.status = :status, o.publishedAt = CURRENT_TIMESTAMP WHERE o.id IN :ids")
    void markAsPublished(@Param("ids") List<Long> ids, @Param("status") OutboxStatus status);

    @Modifying
    @Query("UPDATE NotificationOutbox o SET o.retryCount = o.retryCount + 1 WHERE o.id IN :ids")
    void incrementRetryCount(@Param("ids") List<Long> ids);
}
