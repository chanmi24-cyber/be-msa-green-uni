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
    , DUPLICATE_EMAIL("M006", "이미 사용 중인 이메일입니다.", HttpStatus.CONFLICT)
    , SAME_STATUS("M007", "현재와 동일한 상태입니다.", HttpStatus.BAD_REQUEST)
    , ALREADY_RETIRED("M008", "이미 퇴사한 회원입니다.", HttpStatus.BAD_REQUEST)
    , ALREADY_DISMISSED("M009", "이미 퇴임한 교수입니다.", HttpStatus.BAD_REQUEST)
    , ALREADY_TERMINATED("M010", "퇴학 또는 자퇴 처리된 학생입니다.", HttpStatus.BAD_REQUEST)
    ;
    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}