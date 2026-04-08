package com.LiveKlass.LiveKlass.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    INVALID_INPUT_VALUE(400, "C001", "잘못된 입력 값입니다."),
    METHOD_NOT_ALLOWED(405, "C002", "허용되지 않은 메소드입니다."),
    INTERNAL_SERVER_ERROR(500, "C003", "서버 내부 오류입니다."),

    NOTIFICATION_NOT_FOUND(404, "N001", "존재하지 않는 알림입니다."),
    OUTBOX_NOT_FOUND(500, "N002", "알림에 대한 Outbox를 찾을 수 없습니다."),
    NOTIFICATION_SEND_FAILED(500, "N003", "알림 전송에 실패했습니다.");

    private final int status;
    private final String code;
    private final String message;
}