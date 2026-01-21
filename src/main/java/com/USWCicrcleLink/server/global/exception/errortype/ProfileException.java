package com.USWCicrcleLink.server.global.exception.errortype;

import com.USWCicrcleLink.server.global.exception.ExceptionType;

public class ProfileException extends BaseException{
    public ProfileException(ExceptionType exceptionType) {
        super(exceptionType);
    }
    public ProfileException(ExceptionType exceptionType, Object additionalData) {
        super(exceptionType, additionalData);
    }
}
