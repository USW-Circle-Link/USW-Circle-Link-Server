package com.USWCicrcleLink.server.global.exception.errortype;

import com.USWCicrcleLink.server.global.exception.ExceptionType;

public class FileException extends BaseException{
    public FileException(ExceptionType exceptionType) {
        super(exceptionType);
    }

    public FileException(ExceptionType exceptionType, Object additionalData) {
        super(exceptionType, additionalData);
    }
}
