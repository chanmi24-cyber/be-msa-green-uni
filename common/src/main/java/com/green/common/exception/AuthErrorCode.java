package com.green.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {
    LOGIN_FAIL("A001", "코드와 비밀번호를 확인해주세요.", HttpStatus.BAD_REQUEST)
    , ACCOUNT_TERMINATED("A002", "로그인 불가 계정입니다.", HttpStatus.BAD_REQUEST)
    , WRONG_PASSWORD("A003", "비밀번호를 확인해 주세요.", HttpStatus.BAD_REQUEST)
    , MEMBER_NOT_FOUND("A004", "존재하지 않는 회원입니다.", HttpStatus.FORBIDDEN)
    , INVALID_REFRESH_TOKEN("A005", "유효하지 않은 토큰입니다.", HttpStatus.UNAUTHORIZED)
    , EXPIRED_TOKEN("A006", "만료된 토큰입니다.", HttpStatus.UNAUTHORIZED)
    ;
    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}