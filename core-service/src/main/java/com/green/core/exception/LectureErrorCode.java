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
    LECTURE_NOT_DELETABLE("L005", "승인 완료된 강의는 삭제할 수 없습니다.", HttpStatus.CONFLICT),
    EXCEED_CLASSROOM_CAPACITY("L006", "수강인원이 강의실 최대 수용인원을 초과합니다.", HttpStatus.BAD_REQUEST),
    SCHEDULE_CONFLICT("L007", "해당 강의실에 같은 시간대 강의가 이미 존재합니다.", HttpStatus.CONFLICT),
    PROFESSOR_SCHEDULE_CONFLICT("L008", "해당 시간에 이미 다른 강의가 있습니다.", HttpStatus.CONFLICT),
    INVALID_DATE_RANGE("L009", "종료일자는 시작일자보다 이후여야 합니다.", HttpStatus.BAD_REQUEST),
    CLASSROOM_NOT_FOUND("L010", "존재하지 않는 강의실입니다.", HttpStatus.NOT_FOUND);


    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}