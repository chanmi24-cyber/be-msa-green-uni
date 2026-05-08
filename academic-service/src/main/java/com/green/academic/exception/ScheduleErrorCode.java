package com.green.academic.exception;

import com.green.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ScheduleErrorCode implements ErrorCode {
    INVALID_DATE_RANGE("SC001", "시작일이 종료일보다 늦을 수 없습니다.", HttpStatus.BAD_REQUEST),
    SCHEDULE_NOT_FOUND("SC002", "존재하지 않는 학사일정입니다.", HttpStatus.NOT_FOUND),
    ;
    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}