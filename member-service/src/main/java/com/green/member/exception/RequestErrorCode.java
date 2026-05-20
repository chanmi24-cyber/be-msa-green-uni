package com.green.member.exception;

import com.green.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum RequestErrorCode implements ErrorCode {
    FILE_TOO_LARGE("R001", "파일 크기는 5MB 이하여야 합니다.", HttpStatus.BAD_REQUEST)
    , INVALID_FILE_TYPE("R002", "서류는 PDF 파일만 업로드 가능합니다.", HttpStatus.BAD_REQUEST)
    , ALREADY_IN_MAJOR("R003", "이미 소속된 학과입니다.", HttpStatus.BAD_REQUEST)
    , ALREADY_PENDING_REQUEST("R004", "이미 처리 중인 신청이 있습니다.", HttpStatus.CONFLICT)
    ;
    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
