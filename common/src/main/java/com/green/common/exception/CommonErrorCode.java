package com.green.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommonErrorCode  implements ErrorCode {
    INVALID_INPUT_VALUE("C001", "잘못된 입력값입니다.", HttpStatus.BAD_REQUEST)
    , INTERNAL_SERVER_ERROR("C002", "서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR)
    , SERVICE_UNAVAILABLE("C003", "현재 서비스를 이용할 수 없습니다.", HttpStatus.SERVICE_UNAVAILABLE)
    , GATEWAY_TIMEOUT("C004", "서비스 응답 시간이 초과되었습니다.", HttpStatus.GATEWAY_TIMEOUT)
    , GATEWAY_INTERNAL_ERROR("C005", "게이트웨이 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR)
    , NOT_FOUND_PATH("C007", "존재하지 않는 경로입니다.", HttpStatus.NOT_FOUND)
    , DUPLICATE_ENTRY("C008", "이미 존재하는 데이터입니다.", HttpStatus.CONFLICT)
    ;

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}