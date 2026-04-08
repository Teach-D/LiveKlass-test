package com.LiveKlass.LiveKlass.global;

import com.LiveKlass.LiveKlass.entity.NotificationOutbox;
import com.LiveKlass.LiveKlass.enums.NotificationStatus;
import com.LiveKlass.LiveKlass.global.NotificationItemService.SendResult;
import com.LiveKlass.LiveKlass.repository.NotificationOutboxRepository;
import com.LiveKlass.LiveKlass.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPoller {

    private final NotificationOutboxRepository outboxRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationItemService notificationItemService;
    private final OutboxPersistenceService persistenceService;

    @Scheduled(fixedDelay = 5_000)
    public void poll() {
        List<NotificationOutbox> claimed = persistenceService.claimPending();
        if (claimed.isEmpty()) return;

        List<NotificationOutbox> toSend  = new ArrayList<>();
        List<Long> skipIds = new ArrayList<>();

        for (NotificationOutbox outbox : claimed) {
            notificationRepository.findById(outbox.getNotificationId()).ifPresent(n -> {
                if (n.getStatus() == NotificationStatus.PROCESSING) {
                    toSend.add(outbox);
                } else {
                    log.info("[Outbox] 이미 처리된 알림 스킵 - notificationId={}, status={}",
                            outbox.getNotificationId(), n.getStatus());
                    skipIds.add(outbox.getId());
                }
            });
        }

        if (!skipIds.isEmpty()) {
            persistenceService.markSkipped(skipIds);
        }

        if (toSend.isEmpty()) return;

        List<CompletableFuture<SendResult>> futures = toSend.stream()
                .map(o -> notificationItemService.sendSingle(o.getNotificationId()))
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        List<Long> successNotifIds = new ArrayList<>();
        List<Long> successOutboxIds = new ArrayList<>();
        List<NotificationOutbox> failedOutboxes = new ArrayList<>();

        for (int i = 0; i < toSend.size(); i++) {
            SendResult result = futures.get(i).getNow(new SendResult(0L, false));
            NotificationOutbox outbox = toSend.get(i);
            if (result.success()) {
                successNotifIds.add(result.id());
                successOutboxIds.add(outbox.getId());
            } else {
                failedOutboxes.add(outbox);
            }
        }

        persistenceService.applyResults(successNotifIds, successOutboxIds, failedOutboxes);

        log.info("[Outbox] 처리 완료 - 성공={}, 실패={} (재시도 대기 포함)",
                successNotifIds.size(), failedOutboxes.size());
    }
}
