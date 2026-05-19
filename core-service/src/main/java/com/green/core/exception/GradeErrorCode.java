package com.green.core.exception;

import com.green.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

// [추가] 성적 도메인 에러 코드
@Getter
@RequiredArgsConstructor
public enum GradeErrorCode implements ErrorCode {

    NOT_GRADE_INPUT_PERIOD("G001", "성적 입력 기간이 아닙니다.", HttpStatus.FORBIDDEN),
    NOT_PROFESSOR_LECTURE("G002", "본인 강의가 아닙니다.", HttpStatus.FORBIDDEN),
    SCORE_OUT_OF_RANGE("G003", "점수는 0~100 범위여야 합니다.", HttpStatus.BAD_REQUEST),
    NOT_GRADE_APPEAL_PERIOD("G010", "성적 이의신청 기간이 아닙니다.", HttpStatus.FORBIDDEN),
    NOT_OWN_GRADE_FOR_APPEAL("G011", "본인 성적이 아닙니다.", HttpStatus.FORBIDDEN),
    APPEAL_ALREADY_EXISTS("G012", "이미 신청된 이의신청입니다.", HttpStatus.CONFLICT),
    APPEAL_ALREADY_PROCESSED("G013", "이미 처리된 이의신청입니다.", HttpStatus.CONFLICT),
    EVALUATION_NOT_COMPLETED("G020", "강의평가를 완료해야 성적을 조회할 수 있습니다.", HttpStatus.FORBIDDEN),
    NOT_OWN_GRADE("G021", "본인 성적이 아닙니다.", HttpStatus.FORBIDDEN),
    GRADE_NOT_FOUND("G022", "존재하지 않는 성적입니다.", HttpStatus.NOT_FOUND),
    APPEAL_NOT_FOUND("G023", "존재하지 않는 이의신청입니다.", HttpStatus.NOT_FOUND),
    APPEAL_NOT_REJECTED("G024", "반려된 이의신청만 재신청 가능합니다.", HttpStatus.BAD_REQUEST),
    REJECT_REASON_REQUIRED("G025", "반려 시 사유를 입력해야 합니다.", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
