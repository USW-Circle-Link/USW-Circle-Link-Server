package com.USWCicrcleLink.server.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.SQLException;

@Aspect
@Component
@Slf4j
public class RepositoryExceptionAspect {
    private final SQLExceptionTranslator exTranslator;

    public RepositoryExceptionAspect(DataSource dataSource) {
        this.exTranslator = new SQLErrorCodeSQLExceptionTranslator(dataSource);
    }
    @AfterThrowing(
            pointcut = "execution(* com.USWCicrcleLink.server.user.repository.*.*(..)) || " +
                       "execution(* com.USWCicrcleLink.server.profile.repository.*.*(..)) || " +
                       "execution(* com.USWCicrcleLink.server.email.repository.*.*(..)) || " +
                       "execution(* com.USWCicrcleLink.server.aplict.repository.*.*(..))"+
                        "execution(* com.USWCicrcleLink.server.club.repository.*.*(..)) || "+
                        "execution(* com.USWCicrcleLink.server.clubLeader.repository.*.*(..)) || "+
                        "execution(* com.USWCicrcleLink.server.admin.repository.*.*(..)) || ",
            throwing = "ex"
    )

    public void logException(JoinPoint joinPoint, Throwable ex) {
        if (ex instanceof SQLException) {
            DataAccessException translatedEx = exTranslator.translate("Repository operation", null, (SQLException) ex);
            throw translatedEx;
        } else {
            // 예외 로깅 등 기타 작업 수행
            log.error("Exception in method: {} with cause: {}", joinPoint.getSignature(), ex.getCause() != null ? ex.getCause() : "NULL");
            log.error("Exception message: {}", ex.getMessage());
        }
    }
}
