package com.green.academic.exception;

import com.green.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AnnouncementErrorCode implements ErrorCode {
    ANNOUNCEMENT_NOT_FOUND("ANNO001", "공지사항을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    ANNOUNCEMENT_ACCESS_DENIED("ANNO002", "해당 공지사항에 접근 권한이 없습니다.", HttpStatus.FORBIDDEN),
    ;
    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}