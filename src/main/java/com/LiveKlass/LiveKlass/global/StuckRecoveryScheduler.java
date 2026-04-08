package com.LiveKlass.LiveKlass.global;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StuckRecoveryScheduler {

    private static final int STUCK_THRESHOLD_MINUTES = 10;

    private final OutboxPersistenceService persistenceService;

    @Scheduled(fixedDelay = 600000)
    public void recover() {
        int recovered = persistenceService.recoverStuckLocked(STUCK_THRESHOLD_MINUTES);
        if (recovered > 0) {
            log.warn("[Recovery] {}건 복구 완료 → OutboxPoller가 재처리 예정", recovered);
        }
    }
}
