package com.USWCicrcleLink.server.global.bucket4j;

import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.RateLimitExceededException;
import com.USWCicrcleLink.server.user.domain.User;
import com.USWCicrcleLink.server.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimiterAspect {

    private final APIRateLimiter apiRateLimiter;
    private final UserService userService;


    @Around("@annotation(rateLimit)")
    public Object rateLimit(ProceedingJoinPoint joinPoint, RateLimite rateLimit) throws Throwable {

        String action = rateLimit.action();
        String clientId="";

        // 로그인 이후의 요청인 경우
        if(action.equals("WITHDRAWAL_EMAIL") || action.equals("WITHDRAWAL_CODE")){
            User user = userService.getUserByAuth();
            clientId = String.valueOf(user.getUserUUID());
        }
        else{  // 로그인 되지 않은 사용자의 요청인 경우
            Object[] args = joinPoint.getArgs();
            for(Object arg:args){
                if(arg instanceof ClientIdentifier identifier){
                    clientId = identifier.getClientId();
                }
                else if(arg instanceof UUID uuid){
                    clientId = String.valueOf(uuid);
                }
            }
        }

        // bucketId 생성
        String bucketId= clientId +":"+ action;

        if (apiRateLimiter.tryConsume(bucketId,RateLimitAction.valueOf(action))) {
            apiRateLimiter.logBucketState(bucketId, RateLimitAction.valueOf(action));
            return joinPoint.proceed();
        } else { // 토큰이 없는 경우 예외 발생
            throw new RateLimitExceededException(ExceptionType.TOO_MANY_ATTEMPT);
        }
    }
}