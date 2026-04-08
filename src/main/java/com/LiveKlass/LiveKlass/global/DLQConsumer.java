package com.LiveKlass.LiveKlass.global;

import com.LiveKlass.LiveKlass.dto.NotificationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DLQConsumer {

    private final OutboxPersistenceService persistenceService;

    @RabbitListener(queues = RabbitMQConfig.DLQ_QUEUE)
    public void handleDead(NotificationMessage message) {
        persistenceService.handleSendFailure(message.notificationId());
    }
}
