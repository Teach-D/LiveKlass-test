package com.LiveKlass.LiveKlass.repository;

import com.LiveKlass.LiveKlass.entity.NotificationOutbox;
import com.LiveKlass.LiveKlass.enums.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationOutboxRepository extends JpaRepository<NotificationOutbox, Long> {

    @Query(value = "SELECT * FROM notification_outbox WHERE status = 'PENDING' FOR UPDATE SKIP LOCKED",
            nativeQuery = true)
    List<NotificationOutbox> findPendingForUpdate();

    @Modifying
    @Query("UPDATE NotificationOutbox o SET o.status = :status WHERE o.id IN :ids")
    void updateStatus(@Param("ids") List<Long> ids, @Param("status") OutboxStatus status);

    @Modifying
    @Query("UPDATE NotificationOutbox o SET o.status = :status, o.publishedAt = CURRENT_TIMESTAMP WHERE o.id IN :ids")
    void markAsPublished(@Param("ids") List<Long> ids, @Param("status") OutboxStatus status);

    @Modifying
    @Query("UPDATE NotificationOutbox o SET o.retryCount = o.retryCount + 1 WHERE o.id IN :ids")
    void incrementRetryCount(@Param("ids") List<Long> ids);
}
