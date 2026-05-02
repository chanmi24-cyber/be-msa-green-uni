package com.green.common.auth;

import com.green.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {
    LOGIN_FAIL("A001", "코드와 비밀번호를 확인해주세요.", HttpStatus.BAD_REQUEST)
    , ACCOUNT_TERMINATED("A002", "로그인 불가 계정입니다.", HttpStatus.BAD_REQUEST)
    , INVALID_REFRESH_TOKEN("A003", "유효하지 않은 토큰입니다.", HttpStatus.UNAUTHORIZED)
    ;
    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}