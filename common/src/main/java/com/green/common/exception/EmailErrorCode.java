package com.green.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum EmailErrorCode implements ErrorCode {
    INVALID_VERIFY_CODE("E001", "인증코드가 일치하지 않습니다. ", HttpStatus.BAD_REQUEST)
    , EXPIRED_VERIFY_CODE("E002", "인증코드가 만료되었습니다.", HttpStatus.BAD_REQUEST)
    , NOT_VERIFIED_EMAIL("E003", "이메일 인증이 필요합니다.", HttpStatus.BAD_REQUEST)
    , MEMBER_EMAIL_NOT_FOUND("E004", "존재하지 않는 회원입니다.", HttpStatus.NOT_FOUND)
    , MAIL_SEND_FAIL("E005", "메일 발송에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR)
    , MAIL_INVALID_ADDRESS("E006", "유효하지 않은 이메일 주소입니다. ", HttpStatus.BAD_REQUEST)
    ;
    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
