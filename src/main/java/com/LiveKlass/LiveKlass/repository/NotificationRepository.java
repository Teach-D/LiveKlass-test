package com.LiveKlass.LiveKlass.repository;

import com.LiveKlass.LiveKlass.entity.Notification;
import com.LiveKlass.LiveKlass.enums.NotificationStatus;
import com.LiveKlass.LiveKlass.enums.SendTimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findAllByUserId(Long userId);
    List<Notification> findAllByUserIdAndIsRead(Long userId, boolean isRead);
    List<Notification> findAllBySendTimeSlotAndStatus(SendTimeSlot slot, NotificationStatus notificationStatus);
}
