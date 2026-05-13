package com.green.core.exception;

import com.green.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MajorErrorCode implements ErrorCode {

    // 400 Bad Request - 필수값 검증
    MAJOR_NAME_REQUIRED("MAJOR_001", "학과명을 입력해주세요.", HttpStatus.BAD_REQUEST),
    COLLEGE_ID_REQUIRED("MAJOR_002", "소속대학을 선택해주세요.", HttpStatus.BAD_REQUEST),
    BUILDING_REQUIRED("MAJOR_003", "건물을 선택해주세요.", HttpStatus.BAD_REQUEST),
    ROOM_REQUIRED("MAJOR_004", "호수를 입력해주세요.", HttpStatus.BAD_REQUEST),
    TEL_REQUIRED("MAJOR_005", "전화번호를 입력해주세요.", HttpStatus.BAD_REQUEST),
    COURSE_DURATION_INVALID("MAJOR_006", "수업연한을 4년 이상으로 입력해주세요.", HttpStatus.BAD_REQUEST),
    CAPACITY_INVALID("MAJOR_007", "입학정원을 30명 이상으로 입력해주세요.", HttpStatus.BAD_REQUEST),
    FOUNDED_DATE_REQUIRED("MAJOR_008", "개설일을 선택해주세요.", HttpStatus.BAD_REQUEST),
    // 404 Not Found
    COLLEGE_NOT_FOUND("MAJOR_009", "존재하지 않는 단과대입니다.", HttpStatus.NOT_FOUND),
    MAJOR_NOT_FOUND("MAJOR_010", "존재하지 않는 학과입니다.", HttpStatus.NOT_FOUND),
    // 409 Conflict - 중복 검증
    MAJOR_NAME_DUPLICATED("MAJOR_011", "이미 존재하는 학과명입니다.", HttpStatus.CONFLICT),
    TEL_DUPLICATED("MAJOR_012", "이미 사용 중인 전화번호입니다.", HttpStatus.CONFLICT),
    CHAIR_PROFESSOR_DUPLICATED("MAJOR_013", "이미 다른 학과의 학과장으로 임명된 교수입니다.", HttpStatus.CONFLICT),
    OFFICE_ROOM_DUPLICATED("MAJOR_014", "이미 사용 중인 학과 사무실입니다.", HttpStatus.CONFLICT);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}