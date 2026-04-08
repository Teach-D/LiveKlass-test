package com.LiveKlass.LiveKlass;

import com.LiveKlass.LiveKlass.entity.NotificationOutbox;
import com.LiveKlass.LiveKlass.enums.NotificationStatus;
import com.LiveKlass.LiveKlass.enums.OutboxStatus;
import com.LiveKlass.LiveKlass.global.OutboxPersistenceService;
import com.LiveKlass.LiveKlass.repository.NotificationOutboxRepository;
import com.LiveKlass.LiveKlass.repository.NotificationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Outbox 재시도 로직 단위 테스트")
class OutboxRetryTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationOutboxRepository outboxRepository;

    @InjectMocks
    private OutboxPersistenceService persistenceService;

    @Test
    @DisplayName("첫 번째 실패 - retryCount 증가, notification 상태 유지")
    void first_failure__increments_retry_count() {
        NotificationOutbox outbox = createOutbox(1L, 10L, 0);
        assertThat(outbox.isExhausted()).isFalse();

        persistenceService.applyResults(List.of(), List.of(), List.of(outbox));

        verify(outboxRepository).incrementRetryCount(List.of(10L));
        verify(notificationRepository, never()).updateStatusByIds(any(), eq(NotificationStatus.FAILED));
    }

    @Test
    @DisplayName("두 번째 실패 (retryCount=1) - 여전히 재시도")
    void second_failure__still_retries() {
        NotificationOutbox outbox = createOutbox(1L, 10L, 1);
        assertThat(outbox.isExhausted()).isFalse();

        persistenceService.applyResults(List.of(), List.of(), List.of(outbox));

        verify(outboxRepository).incrementRetryCount(List.of(10L));
        verify(notificationRepository, never()).updateStatusByIds(any(), eq(NotificationStatus.FAILED));
    }

    @Test
    @DisplayName("세 번째 실패 (retryCount=2) - 여전히 재시도")
    void third_failure__still_retries() {
        NotificationOutbox outbox = createOutbox(1L, 10L, 2);
        assertThat(outbox.isExhausted()).isFalse();

        persistenceService.applyResults(List.of(), List.of(), List.of(outbox));

        verify(outboxRepository).incrementRetryCount(List.of(10L));
        verify(notificationRepository, never()).updateStatusByIds(any(), eq(NotificationStatus.FAILED));
    }

    @Test
    @DisplayName("재시도 실패")
    void exhausted__marks_failed_and_published() {
        NotificationOutbox outbox = createOutbox(1L, 10L, NotificationOutbox.MAX_RETRIES);
        assertThat(outbox.isExhausted()).isTrue();

        persistenceService.applyResults(List.of(), List.of(), List.of(outbox));

        verify(notificationRepository).updateStatusByIds(List.of(1L), NotificationStatus.FAILED);
        verify(outboxRepository).markAsPublished(List.of(10L), OutboxStatus.PUBLISHED);
        verify(outboxRepository, never()).incrementRetryCount(any());
    }

    private NotificationOutbox createOutbox(Long notificationId, Long outboxId, int retryCount) {
        try {
            NotificationOutbox outbox = NotificationOutbox.create(notificationId);
            setField(outbox, "id", outboxId);
            setField(outbox, "retryCount", retryCount);
            return outbox;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
