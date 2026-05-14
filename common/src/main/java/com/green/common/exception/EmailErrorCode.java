package com.green.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum EmailErrorCode implements ErrorCode {
    INVALID_VERIFY_CODE("E001", "인증코드가 일치하지 않습니다. ", HttpStatus.BAD_REQUEST)
    , EXPIRED_VERIFY_CODE("E002", "인증코드가 만료되었습니다.", HttpStatus.BAD_REQUEST)
    , NOT_VERIFIED_EMAIL("E003", "본인 인증 유효시간이 지났습니다. 재인증 해주세요.", HttpStatus.BAD_REQUEST)
    , MAIL_INVALID_ADDRESS("E004", "유효하지 않은 이메일 주소입니다. ", HttpStatus.BAD_REQUEST)
    , MAIL_SEND_FAIL("E005", "메일 발송에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR)
    ;
    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
