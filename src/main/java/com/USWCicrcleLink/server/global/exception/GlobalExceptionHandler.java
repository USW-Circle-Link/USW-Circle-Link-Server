package com.USWCicrcleLink.server.global.exception;

import com.USWCicrcleLink.server.global.exception.errortype.BaseException;
import com.USWCicrcleLink.server.global.response.ErrorResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    /**
     * 공통 ErrorResponse 생성 메서드
     */
    private ErrorResponse buildErrorResponse(String exception,
                                             String code,
                                             String message,
                                             HttpStatus status,
                                             Object additionalData) {

        return ErrorResponse.builder()
                .exception(exception)
                .code(code)
                .message(message)
                .status(status.value())
                .error(status.getReasonPhrase())
                .additionalData(additionalData)
                .build();
    }

    /**
     * 운영 환경(production)에서는 4xx 에러 로그를 남기지 않음
     */
    private void logByHttpStatus(HttpStatus status, String logMessage, Throwable e, HttpServletRequest request) {
        String requestInfo = String.format("Request: %s %s", request.getMethod(), request.getRequestURI());

        boolean isProduction = "prod".equals(activeProfile);

        if (status.is4xxClientError() && isProduction) {
            return;
        }

        if (status.is4xxClientError()) {
            log.warn("[Client Error] {} | {}", logMessage, requestInfo);
        } else if (status.is5xxServerError()) {
            log.error("[Server Error] {} | {}", logMessage, requestInfo, e);
        }
    }

    /**
     * 처리되지 않은 모든 예외 핸들러 (기본적으로 500 에러 처리)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ErrorResponse errorResponse = buildErrorResponse(
                e.getClass().getSimpleName(),
                "NO_CATCH_ERROR",
                e.getMessage(),
                status,
                null
        );

        logByHttpStatus(status, e.getMessage(), e, request);
        return new ResponseEntity<>(errorResponse, status);
    }

    /**
     * 프로젝트에서 사용하는 BaseException 처리
     * ExceptionType에 따라 4xx/5xx를 구분하여 로깅 및 응답 설정
     */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException e, HttpServletRequest request) {
        ExceptionType exceptionType = e.getExceptionType();
        HttpStatus status = exceptionType.getStatus();

        logByHttpStatus(status, exceptionType.getMessage(), e, request);

        ErrorResponse errorResponse = buildErrorResponse(
                e.getClass().getSimpleName(),
                exceptionType.getCode(),
                exceptionType.getMessage(),
                status,
                e.getAdditionalData()
        );

        return new ResponseEntity<>(errorResponse, status);
    }

    /**
     * @Valid 검증 실패 (예: @NotBlank) 처리 - 400 에러
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        HttpStatus status = HttpStatus.BAD_REQUEST;
        logByHttpStatus(status, "Validation failed: " + fieldErrors, ex, request);

        ErrorResponse errorResponse = buildErrorResponse(
                ex.getClass().getSimpleName(),
                "INVALID_ARGUMENT",
                "입력 값 검증에 실패했습니다.",
                status,
                fieldErrors
        );

        return new ResponseEntity<>(errorResponse, status);
    }

    /**
     * UUID 변환 예외 처리 - 잘못된 UUID 형식일 경우 400 반환
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleUUIDTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        Throwable rootCause = NestedExceptionUtils.getRootCause(ex);
        String errorMessage = (rootCause instanceof IllegalArgumentException)
                ? "유효하지 않은 UUID 형식입니다. 올바른 UUID를 입력하세요."
                : "잘못된 요청입니다.";

        HttpStatus status = HttpStatus.BAD_REQUEST;
        logByHttpStatus(status, errorMessage, ex, request);

        ErrorResponse errorResponse = buildErrorResponse(
                ex.getClass().getSimpleName(),
                "INVALID_UUID_FORMAT",
                errorMessage,
                status,
                null
        );

        return new ResponseEntity<>(errorResponse, status);
    }

    /**
     * 잘못된 JSON 형식 / ENUM 파싱 실패 등 처리 - 400 에러
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException e, HttpServletRequest request) {
        Throwable rootCause = NestedExceptionUtils.getRootCause(e);
        String errorMessage = (rootCause instanceof IllegalArgumentException)
                ? rootCause.getMessage()
                : "유효하지 않은 JSON 혹은 ENUM 값입니다. 올바른 값을 입력하세요.";

        HttpStatus status = HttpStatus.BAD_REQUEST;
        logByHttpStatus(status, errorMessage, e, request);

        ErrorResponse errorResponse = buildErrorResponse(
                "HttpMessageNotReadableException",
                "INVALID_REQUEST_BODY",
                errorMessage,
                status,
                null
        );

        return new ResponseEntity<>(errorResponse, status);
    }

    /**
     * NullPointerException 처리 - 서버 오류로 간주 (500)
     */
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ErrorResponse> handleNullPointerException(NullPointerException e, HttpServletRequest request) {
        Throwable rootCause = NestedExceptionUtils.getRootCause(e);
        String errorMessage = (rootCause != null)
                ? "NullPointerException 발생: " + rootCause.getMessage()
                : "서버 로직 처리 중 NPE가 발생했습니다.";

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        logByHttpStatus(status, errorMessage, e, request);

        ErrorResponse errorResponse = buildErrorResponse(
                e.getClass().getSimpleName(),
                "NULL_POINTER_EXCEPTION",
                errorMessage,
                status,
                null
        );

        return new ResponseEntity<>(errorResponse, status);
    }

    /**
     * 잘못된 경로 요청 처리 - 404 에러
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(NoResourceFoundException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        logByHttpStatus(status, e.getMessage(), e, request);
        ErrorResponse errorResponse = buildErrorResponse(
                "NoResourceFoundException",
                "RESOURCE_NOT_FOUND",
                "요청하신 경로를 찾을 수 없습니다.",
                status,
                null
        );

        return new ResponseEntity<>(errorResponse, status);
    }

    /**
     * 데이터 무결성 위반 예외 처리 (예: 중복 데이터 삽입) - 409 에러
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.CONFLICT;
        String errorMessage = "데이터 무결성 위반 오류가 발생했습니다.";
        logByHttpStatus(status, errorMessage, e, request);

        ErrorResponse errorResponse = buildErrorResponse(
                e.getClass().getSimpleName(),
                "DATA_INTEGRITY_VIOLATION",
                errorMessage,
                status,
                null
        );

        return new ResponseEntity<>(errorResponse, status);
    }

    /**
     * SQL 예외 처리 - 500 에러
     */
    @ExceptionHandler(SQLException.class)
    public ResponseEntity<ErrorResponse> handleSQLException(SQLException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String errorMessage = "데이터베이스 오류가 발생했습니다.";
        logByHttpStatus(status, errorMessage, e, request);

        ErrorResponse errorResponse = buildErrorResponse(
                e.getClass().getSimpleName(),
                "SQL_EXCEPTION",
                errorMessage,
                status,
                null
        );

        return new ResponseEntity<>(errorResponse, status);
    }

    // 파일 크기 10MB 초과시
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        ErrorResponse errorResponse = buildErrorResponse(
                e.getClass().getSimpleName(),
                "MAX_UPLOAD_SIZE_EXCEEDED",
                "업로드 가능한 최대 파일 크기를 초과했습니다. (개별 파일 10MB, 총 파일 크기 50MB)",
                HttpStatus.BAD_REQUEST,
                null
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * [추가 1] DB에서 엔티티를 찾지 못했을 때 - 404 에러
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(EntityNotFoundException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        logByHttpStatus(status, e.getMessage(), e, request);

        ErrorResponse errorResponse = buildErrorResponse(
                e.getClass().getSimpleName(),
                "ENTITY_NOT_FOUND",
                e.getMessage(),
                status,
                null
        );

        return new ResponseEntity<>(errorResponse, status);
    }

    /**
     * [추가 2] 잘못된 비즈니스 로직 요청 - 400 에러
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        logByHttpStatus(status, e.getMessage(), e, request);

        ErrorResponse errorResponse = buildErrorResponse(
                e.getClass().getSimpleName(),
                "INVALID_INPUT_VALUE",
                e.getMessage(),
                status,
                null
        );

        return new ResponseEntity<>(errorResponse, status);
    }

}
