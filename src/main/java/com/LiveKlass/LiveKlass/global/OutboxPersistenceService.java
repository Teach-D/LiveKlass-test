package com.LiveKlass.LiveKlass.global;

import com.LiveKlass.LiveKlass.entity.NotificationOutbox;
import com.LiveKlass.LiveKlass.enums.NotificationStatus;
import com.LiveKlass.LiveKlass.enums.OutboxStatus;
import com.LiveKlass.LiveKlass.repository.NotificationOutboxRepository;
import com.LiveKlass.LiveKlass.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxPersistenceService {

    private final NotificationRepository notificationRepository;
    private final NotificationOutboxRepository outboxRepository;

    @Transactional
    public List<NotificationOutbox> claimPending() {
        List<NotificationOutbox> pending = outboxRepository.findPendingForUpdate();
        if (pending.isEmpty()) return pending;

        List<Long> ids = pending.stream().map(NotificationOutbox::getId).toList();
        outboxRepository.updateStatus(ids, OutboxStatus.LOCKED);
        return pending;
    }

    @Transactional
    public void markSkipped(List<Long> outboxIds) {
        outboxRepository.markAsPublished(outboxIds, OutboxStatus.PUBLISHED);
    }

    @Transactional
    public void applyResults(List<Long> successNotifIds, List<Long> successOutboxIds,
                             List<NotificationOutbox> failedOutboxes) {
        if (!successNotifIds.isEmpty()) {
            notificationRepository.updateStatusByIds(successNotifIds, NotificationStatus.SUCCESS);
            outboxRepository.markAsPublished(successOutboxIds, OutboxStatus.PUBLISHED);
        }

        if (failedOutboxes.isEmpty()) return;

        List<Long> retryOutboxIds = new ArrayList<>();
        List<Long> exhaustedNotifIds = new ArrayList<>();
        List<Long> exhaustedOutboxIds = new ArrayList<>();

        for (NotificationOutbox outbox : failedOutboxes) {
            if (outbox.isExhausted()) {
                exhaustedNotifIds.add(outbox.getNotificationId());
                exhaustedOutboxIds.add(outbox.getId());
                log.warn("[Outbox] 재시도 횟수 소진 - notificationId={}, retryCount={}",
                        outbox.getNotificationId(), outbox.getRetryCount());
            } else {
                retryOutboxIds.add(outbox.getId());
                log.info("[Outbox] 재시도 예약 - notificationId={}, retryCount={}/{}",
                        outbox.getNotificationId(), outbox.getRetryCount() + 1, NotificationOutbox.MAX_RETRIES);
            }
        }

        if (!retryOutboxIds.isEmpty()) {
            outboxRepository.incrementRetryCount(retryOutboxIds);
            outboxRepository.updateStatus(retryOutboxIds, OutboxStatus.PENDING);
        }
        if (!exhaustedNotifIds.isEmpty()) {
            notificationRepository.updateStatusByIds(exhaustedNotifIds, NotificationStatus.FAILED);
            outboxRepository.markAsPublished(exhaustedOutboxIds, OutboxStatus.PUBLISHED);
        }
    }
}
