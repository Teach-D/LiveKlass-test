package com.LiveKlass.LiveKlass.global;

import com.LiveKlass.LiveKlass.dto.NotificationMessage;
import com.LiveKlass.LiveKlass.exception.BusinessException;
import com.LiveKlass.LiveKlass.exception.ErrorCode;
import com.LiveKlass.LiveKlass.global.NotificationItemService.SendResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final NotificationItemService notificationItemService;
    private final OutboxPersistenceService persistenceService;

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void consume(NotificationMessage message) {
        Long notificationId = message.notificationId();
        SendResult result = notificationItemService.sendSingle(notificationId).join();

        if (result.success()) {
            persistenceService.onSendSuccess(notificationId);
        } else {
            log.warn("[Consumer] 알림 전송 실패 - notificationId={}", notificationId);
            throw new BusinessException(ErrorCode.NOTIFICATION_SEND_FAILED);
        }
    }
}
