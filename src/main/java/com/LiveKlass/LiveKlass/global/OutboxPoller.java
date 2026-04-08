package com.LiveKlass.LiveKlass.global;

import com.LiveKlass.LiveKlass.dto.NotificationMessage;
import com.LiveKlass.LiveKlass.entity.Notification;
import com.LiveKlass.LiveKlass.entity.NotificationOutbox;
import com.LiveKlass.LiveKlass.enums.NotificationStatus;
import com.LiveKlass.LiveKlass.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPoller {

    private final NotificationRepository notificationRepository;
    private final OutboxPersistenceService persistenceService;
    private final RabbitTemplate rabbitTemplate;

    @Scheduled(fixedDelay = 5000)
    public void poll() {
        List<NotificationOutbox> claimed = persistenceService.claimPending();
        if (claimed.isEmpty()) return;

        List<Long> inQueueIds = new ArrayList<>();
        List<Long> skippedIds = new ArrayList<>();
        List<NotificationOutbox> failedToPublish = new ArrayList<>();

        for (NotificationOutbox outbox : claimed) {
            switch (tryPublish(outbox)) {
                case IN_QUEUE -> inQueueIds.add(outbox.getId());
                case SKIPPED -> skippedIds.add(outbox.getId());
                case FAILED -> failedToPublish.add(outbox);
            }
        }

        if (!inQueueIds.isEmpty()) persistenceService.markInQueue(inQueueIds);
        if (!skippedIds.isEmpty()) persistenceService.markPublished(skippedIds);
        if (!failedToPublish.isEmpty()) persistenceService.applyPublishFailures(failedToPublish);
    }

    private PublishResult tryPublish(NotificationOutbox outbox) {
        Long notificationId = outbox.getNotificationId();

        Notification notification = notificationRepository.findById(notificationId).orElse(null);
        if (notification == null) {
            return PublishResult.SKIPPED;
        }
        if (notification.getStatus() != NotificationStatus.PROCESSING) {
            return PublishResult.SKIPPED;
        }

        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.NOTIFICATION_EXCHANGE,
                    RabbitMQConfig.NOTIFICATION_KEY,
                    new NotificationMessage(notificationId)
            );
            return PublishResult.IN_QUEUE;
        } catch (Exception e) {
            return PublishResult.FAILED;
        }
    }

    private enum PublishResult { IN_QUEUE, SKIPPED, FAILED }
}
