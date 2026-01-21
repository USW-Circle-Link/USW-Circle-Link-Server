package com.USWCicrcleLink.server.global.exception.errortype;

import com.USWCicrcleLink.server.global.exception.ExceptionType;
import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {

    private final ExceptionType exceptionType;
    private final Object additionalData; // 예외처리 + 데이터

    public BaseException(ExceptionType exceptionType) {
        super(exceptionType.getMessage());
        this.exceptionType = exceptionType;
        this.additionalData = null;
    }

    public BaseException(ExceptionType exceptionType, Object additionalData) {
        super(exceptionType.getMessage());
        this.exceptionType = exceptionType;
        this.additionalData = additionalData;
    }
}