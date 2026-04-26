package com.green.common.exception;

import com.green.common.model.ResultResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.security.SignatureException;
import java.util.ArrayList;
import java.util.List;

// 프로젝트 전역 예외 처리기
// 컨트롤러에서 발생하는 다양한 예외를 가로채 공통된 응답 형식(ResultResponse)으로 반환
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    //Validation 예외가 발생되었을 때 캐치
    //입력값 검증 실패 시 호출. 여러 에러 중 첫 번째 에러 메시지만 추출하여 반환
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex
            , HttpHeaders headers
            , HttpStatusCode statusCode
            , WebRequest request) {
        List<ValidationError> errors = getValidationError(ex);
//        List<String> messages = errors.stream()
//                .map(item -> item.getMessage())
//                .toList(); // 발생한 모든 검증 에러 리스트 추출 (필요시 주석 해제)
//        String result = String.join("\n", messages);
        String message = ex.getBindingResult()
                .getFieldErrors()
                .get(0)                    // 첫번째 에러만 반환
                .getDefaultMessage();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ResultResponse<>(message, null));
    }

    // 일반적인 모든 예외(Exception) 처리
    // 예상치 못한 서버 내부 오류 발생 시 로그를 남기고 500 에러를 반환
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResultResponse<String>> handleException(Exception ex) {
        log.error("Exception: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResultResponse<>("서버 오류가 발생했습니다.", ex.getMessage()));
    }

    // ResponseStatusException 예외 처리
    // 의도적으로 던진 상태 코드 예외(ex: 404 Not Found)를 처리
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ResultResponse<String>> handleResponseStatusException(ResponseStatusException ex) {
        log.error("ResponseStatusException: {}", ex.getReason());

        String statusMessage = HttpStatus.valueOf(ex.getStatusCode().value()).getReasonPhrase();

        return ResponseEntity.status(ex.getStatusCode())
                .body(new ResultResponse<>(ex.getReason(), null));
    }

    // JWT 토큰에 문제 발생시
    // 잘못된 형식의 토큰이거나 서명이 일치하지 않을 때 401 Unauthorized를 반환
    @ExceptionHandler({MalformedJwtException.class, SignatureException.class})
    public ResponseEntity<Object> handleMalformedJwtException() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ResultResponse<>("토큰을 확인해 주세요.", null));
    }
    // JWT 토큰이 만료가 되었을 때
    // 유효 기간이 지난 토큰으로 요청 시 401 Unauthorized를 반환
    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<Object> handleExpiredJwtException() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ResultResponse<>("토큰을 확인해 주세요.", null));
    }

    // BindException에서 필드 에러 리스트를 ValidationError 객체 리스트로 변환
    // @param e 검증 오류가 담긴 예외 객체
    // @return 커스텀 ValidationError 리스트
    private List<ValidationError> getValidationError(BindException e) {
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();

        List<ValidationError> result = new ArrayList<>(fieldErrors.size());
        for(FieldError fieldError : fieldErrors){
            result.add(ValidationError.of(fieldError));
        }
        return result;
        //return fieldErrors.stream().map(item -> ValidationError.of(item)).toList();
    }
}
