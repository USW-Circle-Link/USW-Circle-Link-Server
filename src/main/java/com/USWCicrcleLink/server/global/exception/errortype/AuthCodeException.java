package com.USWCicrcleLink.server.global.exception.errortype;

import com.USWCicrcleLink.server.global.exception.ExceptionType;

public class AuthCodeException extends BaseException{
    public AuthCodeException(ExceptionType exceptionType) {
        super(exceptionType);
    }
}
