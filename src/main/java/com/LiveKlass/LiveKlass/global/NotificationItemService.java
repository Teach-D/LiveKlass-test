package com.LiveKlass.LiveKlass.global;

import com.LiveKlass.LiveKlass.entity.Notification;
import com.LiveKlass.LiveKlass.enums.NotificationStatus;
import com.LiveKlass.LiveKlass.exception.BusinessException;
import com.LiveKlass.LiveKlass.exception.ErrorCode;
import com.LiveKlass.LiveKlass.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationItemService {

    private final NotificationRepository notificationRepository;

    @Async("notificationExecutor")
    @Transactional
    public CompletableFuture<Void> sendSingle(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND));
        try {
            log.info("[id={}] 전송 - thread: {}", notificationId, Thread.currentThread().getName());
            Thread.sleep(20);
            notification.updateStatus(NotificationStatus.SUCCESS);
        } catch (Exception e) {
            notification.updateStatus(NotificationStatus.FAILED);
        }
        return CompletableFuture.completedFuture(null);
    }
}
