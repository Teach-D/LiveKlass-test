package com.LiveKlass.LiveKlass.service;

import com.LiveKlass.LiveKlass.dto.request.NotificationRequest;
import com.LiveKlass.LiveKlass.dto.response.NotificationResponse;
import com.LiveKlass.LiveKlass.entity.Notification;
import com.LiveKlass.LiveKlass.enums.NotificationStatus;
import com.LiveKlass.LiveKlass.exception.BusinessException;
import com.LiveKlass.LiveKlass.exception.ErrorCode;
import com.LiveKlass.LiveKlass.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public void registerNotification(NotificationRequest request) {
        List<Notification> notifications = request.getChannels().stream()
                .map(channel -> Notification.builder()
                        .userId(request.getUserId())
                        .type(request.getType())
                        .eventId(request.getEventId())
                        .channel(channel)
                        .status(NotificationStatus.PENDING)
                        .build())
                .toList();

        notificationRepository.saveAll(notifications);
    }

    @Transactional(readOnly = true)
    public NotificationResponse getNotificationStatus(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND));

        return NotificationResponse.builder()
                .id(notification.getId())
                .eventId(notification.getEventId())
                .status(notification.getStatus())
                .channel(notification.getChannel())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}