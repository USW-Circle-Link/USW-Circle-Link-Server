package com.USWCicrcleLink.server.global.exception.errortype;

import com.USWCicrcleLink.server.global.exception.ExceptionType;

public class WithdrawalTokenException extends BaseException{
    public WithdrawalTokenException (ExceptionType exceptionType) {
        super(exceptionType);
    }
}
