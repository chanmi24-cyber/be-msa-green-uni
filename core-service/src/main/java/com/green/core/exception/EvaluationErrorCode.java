package com.green.core.exception;

import com.green.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum EvaluationErrorCode implements ErrorCode {
    EVALUATION_NOT_FOUND("E001", "존재하지 않는 강의평가입니다.", HttpStatus.NOT_FOUND),
    EVALUATION_ALREADY_EXISTS("E002", "이미 평가한 강의입니다.", HttpStatus.CONFLICT),
    EVAL_PERIOD_NOT_OPEN("E003", "강의평가 기간이 아닙니다.", HttpStatus.FORBIDDEN);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}