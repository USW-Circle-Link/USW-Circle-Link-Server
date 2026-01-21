package com.USWCicrcleLink.server.user.service;

import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.UserException;
import com.USWCicrcleLink.server.user.dto.PasswordRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class PasswordService {

    // 비밀번호 조건
    private static final Pattern letterPattern = Pattern.compile("[a-zA-Z]");
    private static final Pattern numberPattern = Pattern.compile("[0-9]");
    private static final Pattern specialCharPattern = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?~`]");

    // 비밀번호 유효성 검사
    @Transactional(readOnly = true)
    public void validatePassword(String password,String confirmPassword) {
        log.debug("비밀번호 유효성 확인 요청 시작");

        // 비밀번호 칸이 공백인지 확인
       checkPasswordFieldBlank(password,confirmPassword);
        // 비밀번호에 특수문자,숫자,영문자가 1개 이상 포함되어있는지 확인
       checkPasswordCondition(password);
        // 두 비밀번호 일치 확인
        checkPasswordMatch(password,confirmPassword);

        log.debug("비밀번호 유효성 검증 완료");
    }

    // 비밀번호 칸이 빈칸인지 확인
    public void checkPasswordFieldBlank(String password, String comfimPw) {
        if (password.trim().isEmpty() || comfimPw.trim().isEmpty()) {
            throw new UserException(ExceptionType.USER_PASSWORD_NOT_INPUT);
        }
    }

    // 비밀번호 조건이 충족되는지 확인
    public void checkPasswordCondition(String password) {
        if (!letterPattern.matcher(password).find() || !numberPattern.matcher(password).find() || !specialCharPattern.matcher(password).find()) {
            throw new UserException(ExceptionType.USER_PASSWORD_CONDITION_FAILED);
        }
    }

    // 비밀번호 일치 확인
    public void checkPasswordMatch(String password, String confirmPw) {
        if (!password.equals(confirmPw)) {
            throw new UserException(ExceptionType.USER_NEW_PASSWORD_NOT_MATCH);
        }
    }

}
