package com.LiveKlass.LiveKlass.global;

import com.LiveKlass.LiveKlass.entity.NotificationOutbox;
import com.LiveKlass.LiveKlass.enums.NotificationStatus;
import com.LiveKlass.LiveKlass.exception.BusinessException;
import com.LiveKlass.LiveKlass.exception.ErrorCode;
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
    public void markInQueue(List<Long> outboxIds) {
        outboxRepository.updateStatus(outboxIds, OutboxStatus.IN_QUEUE);
    }

    @Transactional
    public void markPublished(List<Long> outboxIds) {
        outboxRepository.markAsPublished(outboxIds, OutboxStatus.PUBLISHED);
    }

    @Transactional
    public void applyPublishFailures(List<NotificationOutbox> failedOutboxes) {
        if (failedOutboxes.isEmpty()) return;

        List<Long> retryIds = new ArrayList<>();
        List<Long> exhaustedNotifIds = new ArrayList<>();
        List<Long> exhaustedOutboxIds = new ArrayList<>();

        for (NotificationOutbox outbox : failedOutboxes) {
            if (outbox.isExhausted()) {
                exhaustedNotifIds.add(outbox.getNotificationId());
                exhaustedOutboxIds.add(outbox.getId());
            } else {
                retryIds.add(outbox.getId());
            }
        }

        if (!retryIds.isEmpty()) {
            outboxRepository.incrementRetryCount(retryIds);
            outboxRepository.updateStatus(retryIds, OutboxStatus.PENDING);
        }
        if (!exhaustedNotifIds.isEmpty()) {
            notificationRepository.updateStatusAndReasonByIds(exhaustedNotifIds,
                    NotificationStatus.FAILED,
                    "RabbitMQ 발행 재시도 횟수(" + NotificationOutbox.MAX_RETRIES + "회) 소진");
            outboxRepository.markAsPublished(exhaustedOutboxIds, OutboxStatus.PUBLISHED);
        }
    }

    @Transactional
    public void onSendSuccess(Long notificationId) {
        notificationRepository.updateStatusByIds(List.of(notificationId), NotificationStatus.SUCCESS);
        outboxRepository.findByNotificationId(notificationId)
                .ifPresent(o -> outboxRepository.markAsPublished(List.of(o.getId()), OutboxStatus.PUBLISHED));
    }

    @Transactional
    public void handleSendFailure(Long notificationId) {
        NotificationOutbox outbox = outboxRepository.findByNotificationId(notificationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.OUTBOX_NOT_FOUND));

        if (outbox.isExhausted()) {
            notificationRepository.updateStatusAndReasonByIds(List.of(notificationId),
                    NotificationStatus.FAILED,
                    "알림 전송 재시도 횟수(" + NotificationOutbox.MAX_RETRIES + "회) 소진");
            outboxRepository.markAsPublished(List.of(outbox.getId()), OutboxStatus.PUBLISHED);
        } else {
            outboxRepository.incrementRetryCount(List.of(outbox.getId()));
            outboxRepository.updateStatus(List.of(outbox.getId()), OutboxStatus.PENDING);
        }
    }
}
