package com.LiveKlass.LiveKlass.global;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class NotificationItemService {

    @Async("notificationExecutor")
    public CompletableFuture<SendResult> sendSingle(Long notificationId) {
        try {
            log.info("[id={}] 전송 - thread: {}", notificationId, Thread.currentThread().getName());
            Thread.sleep(20); // 실제 외부 전송 로직으로 대체
            return CompletableFuture.completedFuture(new SendResult(notificationId, true));
        } catch (Exception e) {
            return CompletableFuture.completedFuture(new SendResult(notificationId, false));
        }
    }

    public record SendResult(Long id, boolean success) {}
}
