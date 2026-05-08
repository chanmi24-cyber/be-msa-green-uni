package com.green.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SchedulePeriodErrorCode implements ErrorCode {
    NOT_COURSE_REGISTRATION_PERIOD("SC003", "수강신청 기간이 아닙니다.", HttpStatus.FORBIDDEN),
    NOT_COURSE_MODIFICATION_PERIOD("SC004", "수강정정 기간이 아닙니다.", HttpStatus.FORBIDDEN),
    NOT_GRADE_INPUT_PERIOD("SC005", "성적입력 기간이 아닙니다.", HttpStatus.FORBIDDEN),
    NOT_GRADE_VIEW_PERIOD("SC006", "성적조회 기간이 아닙니다.", HttpStatus.FORBIDDEN),
    NOT_GRADE_APPEAL_PERIOD("SC007", "성적이의신청 기간이 아닙니다.", HttpStatus.FORBIDDEN),
    NOT_LECTURE_EVALUATION_PERIOD("SC008", "강의평가 기간이 아닙니다.", HttpStatus.FORBIDDEN),
    NOT_TUITION_PAYMENT_PERIOD("SC009", "등록금납부 기간이 아닙니다.", HttpStatus.FORBIDDEN),
    NOT_COURSE_OPEN_PERIOD("SC010", "강의개설신청 기간이 아닙니다.", HttpStatus.FORBIDDEN)
    ;
    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}