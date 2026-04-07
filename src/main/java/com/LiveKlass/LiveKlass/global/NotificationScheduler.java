package com.LiveKlass.LiveKlass.global;

import com.LiveKlass.LiveKlass.enums.SendTimeSlot;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final NotificationSendService notificationSendService;

    @Scheduled(cron = "0 46 07 * * *")
    public void sendMorningNotifications() {
        notificationSendService.processScheduledNotifications(SendTimeSlot.MORNING);
    }

    @Scheduled(cron = "0 0 16 * * *")
    public void sendAfternoonNotifications() {
        notificationSendService.processScheduledNotifications(SendTimeSlot.AFTERNOON);
    }

    @Scheduled(cron = "0 35 22 * * *")
    public void sendEveningNotifications() {
        notificationSendService.processScheduledNotifications(SendTimeSlot.EVENING);
    }
}
