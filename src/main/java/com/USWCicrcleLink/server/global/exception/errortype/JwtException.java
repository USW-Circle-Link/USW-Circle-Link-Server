package com.USWCicrcleLink.server.global.exception.errortype;

import com.USWCicrcleLink.server.global.exception.ExceptionType;

public class JwtException extends BaseException{
    public JwtException (ExceptionType exceptionType) {
        super(exceptionType);
    }
}
