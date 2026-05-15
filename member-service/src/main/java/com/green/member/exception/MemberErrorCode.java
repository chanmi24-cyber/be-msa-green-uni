package com.green.member.exception;

import com.green.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MemberErrorCode implements ErrorCode {
    MEMBER_NOT_FOUND("M001", "존재하지 않는 회원입니다.", HttpStatus.NOT_FOUND)
    , STUDENT_NOT_FOUND("M002", "존재하지 않는 학생입니다.", HttpStatus.NOT_FOUND)
    , PROFESSOR_NOT_FOUND("M003", "존재하지 않는 교수입니다.", HttpStatus.NOT_FOUND)
    , ADMIN_NOT_FOUND("M004", "존재하지 않는 관리자입니다.", HttpStatus.NOT_FOUND)
    , MAJOR_NOT_FOUND("M005", "존재하지 않는 학과입니다.", HttpStatus.NOT_FOUND)
    ;
    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}