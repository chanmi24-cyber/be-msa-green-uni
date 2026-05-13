package com.green.core.exception;

import com.green.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CourseErrorCode implements ErrorCode {

    // 404 Not Found
    STUDENT_NOT_FOUND("COURSE_001", "학생 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    LECTURE_NOT_FOUND("COURSE_002", "존재하지 않는 강의입니다.", HttpStatus.NOT_FOUND),
    COURSE_NOT_FOUND("COURSE_003", "수강 신청한 강의가 아닙니다.", HttpStatus.NOT_FOUND),
    // 409 Conflict
    COURSE_ALREADY_ENROLLED("COURSE_004", "이미 신청된 강의입니다.", HttpStatus.CONFLICT),
    COURSE_SCHEDULE_CONFLICT("COURSE_005", "시간표가 중복됩니다.", HttpStatus.CONFLICT),
    COURSE_CAPACITY_EXCEEDED("COURSE_006", "수강 정원이 초과되었습니다.", HttpStatus.CONFLICT),
    // 422 Unprocessable Entity
    LECTURE_NOT_APPROVED("COURSE_007", "수강 신청 가능한 강의가 아닙니다.", HttpStatus.UNPROCESSABLE_ENTITY),
    MAJOR_NOT_MATCHED("COURSE_008", "신청 대상 학과가 아닙니다.", HttpStatus.UNPROCESSABLE_ENTITY),
    ACADEMIC_YEAR_NOT_MATCHED("COURSE_009", "신청 대상 학년이 아닙니다.", HttpStatus.UNPROCESSABLE_ENTITY),
    CREDIT_LIMIT_EXCEEDED("COURSE_010", "최대 신청 학점(18학점)을 초과합니다.", HttpStatus.UNPROCESSABLE_ENTITY),
    // forbidden
    LIBERAL_ARTS_MAJOR_RESTRICTED("COURSE_011", "해당 교양선택 과목은 소속 학과 학생이 수강할 수 없습니다.", HttpStatus.FORBIDDEN);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}