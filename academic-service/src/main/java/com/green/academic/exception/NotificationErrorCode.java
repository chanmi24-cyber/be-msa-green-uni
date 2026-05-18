package com.green.academic.exception;

import com.green.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum NotificationErrorCode implements ErrorCode {
    NOTIFICATION_NOT_FOUND("NT001", "존재하지 않는 알림입니다.", HttpStatus.NOT_FOUND),
    NOTIFICATION_ACCESS_DENIED("NT002", "본인 알림이 아닙니다.", HttpStatus.FORBIDDEN),
    ;
    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
