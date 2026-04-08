package com.LiveKlass.LiveKlass.global;

import com.LiveKlass.LiveKlass.entity.Notification;
import com.LiveKlass.LiveKlass.entity.NotificationOutbox;
import com.LiveKlass.LiveKlass.enums.NotificationStatus;
import com.LiveKlass.LiveKlass.enums.SendTimeSlot;
import com.LiveKlass.LiveKlass.repository.NotificationOutboxRepository;
import com.LiveKlass.LiveKlass.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationSendService {

    private final NotificationRepository notificationRepository;
    private final NotificationOutboxRepository outboxRepository;

    @Transactional
    public void processScheduledNotifications(SendTimeSlot slot) {
        List<Notification> pending = notificationRepository
                .findAllBySendTimeSlotAndStatus(slot, NotificationStatus.PENDING);

        if (pending.isEmpty()) {
            log.info("[{}] 처리할 PENDING 알림 없음", slot);
            return;
        }

        List<NotificationOutbox> outboxEntries = pending.stream()
                .map(n -> NotificationOutbox.create(n.getId()))
                .toList();
        outboxRepository.saveAll(outboxEntries);

        List<Long> ids = pending.stream().map(Notification::getId).toList();
        notificationRepository.updateStatusByIds(ids, NotificationStatus.PROCESSING);

        log.info("[{}] Outbox {}건 등록 완료", slot, outboxEntries.size());
    }
}
