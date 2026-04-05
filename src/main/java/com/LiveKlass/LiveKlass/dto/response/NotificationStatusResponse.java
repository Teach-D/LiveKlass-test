package com.LiveKlass.LiveKlass.dto.response;

import com.LiveKlass.LiveKlass.enums.NotificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class NotificationStatusResponse {

    private Long id;
    private NotificationStatus status;
}