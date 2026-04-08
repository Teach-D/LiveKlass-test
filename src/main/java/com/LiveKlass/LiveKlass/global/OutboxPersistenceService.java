package com.LiveKlass.LiveKlass.global;

import com.LiveKlass.LiveKlass.enums.NotificationStatus;
import com.LiveKlass.LiveKlass.enums.OutboxStatus;
import com.LiveKlass.LiveKlass.repository.NotificationOutboxRepository;
import com.LiveKlass.LiveKlass.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OutboxPersistenceService {

    private final NotificationRepository notificationRepository;
    private final NotificationOutboxRepository outboxRepository;

    @Transactional
    public void markSkipped(List<Long> outboxIds) {
        outboxRepository.markAsPublished(outboxIds, OutboxStatus.PUBLISHED);
    }

    @Transactional
    public void applyResults(List<Long> successIds, List<Long> failedIds, List<Long> outboxIds) {
        if (!successIds.isEmpty()) notificationRepository.updateStatusByIds(successIds, NotificationStatus.SUCCESS);
        if (!failedIds.isEmpty())  notificationRepository.updateStatusByIds(failedIds,  NotificationStatus.FAILED);
        outboxRepository.markAsPublished(outboxIds, OutboxStatus.PUBLISHED);
    }
}
