package com.USWCicrcleLink.server.global.exception.errortype;

import com.USWCicrcleLink.server.global.exception.ExceptionType;

public class RateLimitExceededException extends BaseException{
    public RateLimitExceededException (ExceptionType exceptionType) {
        super(exceptionType);
    }
}
