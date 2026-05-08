package com.green.core.exception;

import com.green.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum LectureErrorCode implements ErrorCode {
    LECTURE_NOT_FOUND("L001", "존재하지 않는 강의입니다.", HttpStatus.NOT_FOUND),
    NOT_COURSE_OPEN_PERIOD("L002", "강의개설 기간이 아닙니다.", HttpStatus.FORBIDDEN),
    LECTURE_FORBIDDEN("L003", "해당 강의에 대한 권한이 없습니다.", HttpStatus.FORBIDDEN),
    LECTURE_NOT_MODIFIABLE("L004", "승인 완료 또는 승인 대기 상태의 강의는 수정할 수 없습니다.", HttpStatus.CONFLICT),
    LECTURE_NOT_DELETABLE("L005", "승인 완료된 강의는 삭제할 수 없습니다.", HttpStatus.CONFLICT);

    ;

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}