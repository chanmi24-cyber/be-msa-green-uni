package com.green.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum FileErrorCode implements ErrorCode {
    FILE_NOT_FOUND("F001", "첨부 파일이 존재하지 않습니다.", HttpStatus.NOT_FOUND)
    , FILE_TOO_LARGE("F002", "파일 크기는 5MB 이하여야 합니다.", HttpStatus.BAD_REQUEST)
    , INVALID_FILE_TYPE("F003", "서류는 PDF, JPG, JPEG, PNG 파일만 업로드 가능합니다.", HttpStatus.BAD_REQUEST)
    , INVALID_IMAGE_TYPE("F004", "사진은 JPG, JPEG, PNG 파일만 업로드 가능합니다.", HttpStatus.BAD_REQUEST)
    ;

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}

