package com.USWCicrcleLink.server.global.exception;

import com.USWCicrcleLink.server.global.exception.errortype.BaseException;
import com.USWCicrcleLink.server.global.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        HttpStatus status = INTERNAL_SERVER_ERROR;
        String code = "NO_CATCH_ERROR";
        String className = e.getClass().getName();
        String message = e.getMessage();

        ErrorResponse errorResponse = ErrorResponse.builder()
                .exception(className.substring(className.lastIndexOf(".") + 1))
                .code(code)
                .message(message)
                .status(status.value())
                .error(status.getReasonPhrase())
                .build();

        log.error("code : {}, message : {}", errorResponse.getCode(), errorResponse.getMessage());

        return new ResponseEntity<>(errorResponse, status);
    }

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException e) {
        String className = e.getClass().getName();
        ExceptionType exceptionType = e.getExceptionType();

        ErrorResponse errorResponse = ErrorResponse.builder()
                .exception(className.substring(className.lastIndexOf(".") + 1))
                .code(exceptionType.getCode())
                .message(exceptionType.getMessage())
                .status(exceptionType.getStatus().value())
                .error(exceptionType.getStatus().getReasonPhrase())
                .build();

        log.error("code : {}, message : {}", errorResponse.getCode(), errorResponse.getMessage());

        return new ResponseEntity<>(errorResponse, exceptionType.getStatus());
    }
    
    // NotBlank 예외 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    // 유효하지 않은 enum값 예외 처리
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String code = "BAD_REQUEST";
        String className = e.getClass().getName();
        String message = "해당 필드에서 지원하지 않는 값 입니다";

        ErrorResponse errorResponse = ErrorResponse.builder()
                .exception(className.substring(className.lastIndexOf(".") + 1))
                .code(code)
                .message(message)
                .status(status.value())
                .error(status.getReasonPhrase())
                .build();

        log.error("code : {}, message : {}", errorResponse.getCode(), errorResponse.getMessage());

        return new ResponseEntity<>(errorResponse, status);
    }



}