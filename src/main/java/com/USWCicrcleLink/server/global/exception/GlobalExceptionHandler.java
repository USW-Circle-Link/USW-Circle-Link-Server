package com.USWCicrcleLink.server.global.exception;

import com.USWCicrcleLink.server.global.exception.errortype.BaseException;
import com.USWCicrcleLink.server.global.response.ErrorResponse;
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
import jakarta.persistence.EntityNotFoundException;

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
                null);

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
                e.getAdditionalData());

        return new ResponseEntity<>(errorResponse, status);
    }

    /**
     * @Valid 검증 실패 (예: @NotBlank) 처리 - 400 에러
     *        "어떤 필드가 누락되었는지" 친절하게 알려줌
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        Map<String, String> fieldErrors = new HashMap<>();
        StringBuilder errorMessageBuilder = new StringBuilder("다음 필수 항목을 확인해 주세요: ");

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
            errorMessageBuilder.append("[").append(fieldName).append("] ");
        });

        HttpStatus status = HttpStatus.BAD_REQUEST;
        logByHttpStatus(status, "Validation failed: " + fieldErrors, ex, request);

        ErrorResponse errorResponse = buildErrorResponse(
                ex.getClass().getSimpleName(),
                "INVALID_ARGUMENT",
                errorMessageBuilder.toString().trim(), // "다음 필수 항목을 확인해 주세요: [email] [password]"
                status,
                fieldErrors);

        return new ResponseEntity<>(errorResponse, status);
    }

    /**
     * 필수 쿼리 파라미터 누락 (예: ?uuid=... 안 보냈을 때)
     */
    @ExceptionHandler(org.springframework.web.bind.MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParams(
            org.springframework.web.bind.MissingServletRequestParameterException ex, HttpServletRequest request) {
        String parameterName = ex.getParameterName();
        String message = String.format("필수 파라미터가 누락되었습니다. '%s' 값을 넣어주세요.", parameterName);

        HttpStatus status = HttpStatus.BAD_REQUEST;
        logByHttpStatus(status, message, ex, request);

        ErrorResponse errorResponse = buildErrorResponse(
                ex.getClass().getSimpleName(),
                "MISSING_PARAMETER",
                message,
                status,
                null);

        return new ResponseEntity<>(errorResponse, status);
    }

    /**
     * 필수 헤더 누락 (예: Authorization 헤더 등)
     */
    @ExceptionHandler(org.springframework.web.bind.MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingHeaders(
            org.springframework.web.bind.MissingRequestHeaderException ex, HttpServletRequest request) {
        String headerName = ex.getHeaderName();
        String message = String.format("필수 헤더가 누락되었습니다. '%s' 헤더를 포함해서 보내주세요.", headerName);

        HttpStatus status = HttpStatus.BAD_REQUEST;
        logByHttpStatus(status, message, ex, request);

        ErrorResponse errorResponse = buildErrorResponse(
                ex.getClass().getSimpleName(),
                "MISSING_HEADER",
                message,
                status,
                null);

        return new ResponseEntity<>(errorResponse, status);
    }

    /**
     * 파일 업로드 용량 초과 핸들링
     * (spring.servlet.multipart.max-file-size 등 설정값 초과 시 발생)
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex,
            HttpServletRequest request) {
        String message = "파일 용량이 너무 큽니다. (최대 20MB)";

        HttpStatus status = HttpStatus.BAD_REQUEST;
        logByHttpStatus(status, message, ex, request);

        ErrorResponse errorResponse = buildErrorResponse(
                ex.getClass().getSimpleName(),
                "FILE_SIZE_EXCEEDED",
                message,
                status,
                null);

        return new ResponseEntity<>(errorResponse, status);
    }
}
