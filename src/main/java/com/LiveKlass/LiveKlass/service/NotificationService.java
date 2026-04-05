package com.LiveKlass.LiveKlass.service;

import com.LiveKlass.LiveKlass.dto.request.NotificationRequest;
import com.LiveKlass.LiveKlass.entity.Notification;
import com.LiveKlass.LiveKlass.enums.NotificationStatus;
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
}