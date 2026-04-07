package com.LiveKlass.LiveKlass.repository;

import com.LiveKlass.LiveKlass.entity.Notification;
import com.LiveKlass.LiveKlass.enums.NotificationStatus;
import com.LiveKlass.LiveKlass.enums.SendTimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findAllByUserId(Long userId);
    List<Notification> findAllByUserIdAndIsRead(Long userId, boolean isRead);
    List<Notification> findAllBySendTimeSlotAndStatus(SendTimeSlot slot, NotificationStatus notificationStatus);

    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.status = :status WHERE n.id IN :ids")
    void updateStatusByIds(@Param("ids") List<Long> ids, @Param("status") NotificationStatus status);
}
