package com.LiveKlass.LiveKlass.global;

import com.LiveKlass.LiveKlass.entity.Notification;
import com.LiveKlass.LiveKlass.enums.NotificationStatus;
import com.LiveKlass.LiveKlass.enums.SendTimeSlot;
import com.LiveKlass.LiveKlass.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationScheduler {

    private final NotificationRepository notificationRepository;

    @Scheduled(cron = "0 0 10 * * *")
    public void sendMorningNotifications() {
        processScheduledNotifications(SendTimeSlot.MORNING);
    }

    @Scheduled(cron = "0 0 16 * * *")
    public void sendAfternoonNotifications() {
        processScheduledNotifications(SendTimeSlot.AFTERNOON);
    }

    @Scheduled(cron = "0 0 20 * * *")
    public void sendEveningNotifications() {
        processScheduledNotifications(SendTimeSlot.EVENING);
    }

    private void processScheduledNotifications(SendTimeSlot slot) {
        List<Notification> pendingList = notificationRepository
                .findAllBySendTimeSlotAndStatus(slot, NotificationStatus.PENDING);

        pendingList.forEach(notification -> {
            try {
                // 전송
                Thread.sleep(200);
                notification.updateStatus(NotificationStatus.SUCCESS);
            } catch (Exception e) {
                notification.updateStatus(NotificationStatus.FAILED);
            }
        });
    }
}