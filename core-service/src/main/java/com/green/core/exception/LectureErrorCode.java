package com.green.core.exception;

import com.green.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum LectureErrorCode implements ErrorCode {
    LECTURE_NOT_FOUND("L001", "존재하지 않는 강의입니다.", HttpStatus.NOT_FOUND)
    ;

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}