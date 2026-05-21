package com.green.member.exception;

import com.green.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum RequestErrorCode implements ErrorCode {
    ALREADY_PENDING_REQUEST("R001", "이미 처리 중인 신청이 있습니다.", HttpStatus.CONFLICT)
    , NOT_MAJOR_REQUEST("R002", "존재하지 않는 신청서입니다.", HttpStatus.NOT_FOUND)
    , NOT_CANCELLABLE("R003", "취소할 수 없는 신청입니다.", HttpStatus.BAD_REQUEST)
    , ALREADY_IN_MAJOR("R004", "이미 소속된 학과입니다.", HttpStatus.BAD_REQUEST)
    , NOT_PROCESSABLE("R005", "이미 처리된 신청입니다.", HttpStatus.BAD_REQUEST)
    , INELIGIBLE_STUDENT_STATUS("R006", "현재 학적 상태로는 신청할 수 없습니다.", HttpStatus.BAD_REQUEST)
    ;
    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
