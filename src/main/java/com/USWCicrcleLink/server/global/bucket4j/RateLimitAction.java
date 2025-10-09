package com.USWCicrcleLink.server.global.bucket4j;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Refill;

import java.time.Duration;

public enum RateLimitAction {

    // 앱 로그인
    APP_LOGIN {
        @Override
        public Bandwidth getLimit() {
            return Bandwidth.classic(2147483647, Refill.intervally(2147483647, Duration.ofMinutes(5)));
        }
    },

    // 웹 로그인
    WEB_LOGIN {
        @Override
        public Bandwidth getLimit() {
            return Bandwidth.classic(2147483647, Refill.intervally(2147483647, Duration.ofMinutes(5)));
        }

    },

    // 회원 가입 요청
    EMAIL_VERIFICATION {
        @Override
        public Bandwidth getLimit() {
            return Bandwidth.classic(2147483647, Refill.intervally(2147483647, Duration.ofMinutes(5)));
        }
    },

    // 아이디 찾기
    ID_FOUND_EMAIL {
        @Override
        public Bandwidth getLimit() {
            return Bandwidth.classic(2147483647, Refill.intervally(2147483647, Duration.ofMinutes(5)));
        }
    },

    // 비밀번호 찾기
    PW_FOUND_EMAIL {
        @Override
        public Bandwidth getLimit() {
            return Bandwidth.classic(2147483647, Refill.intervally(2147483647, Duration.ofMinutes(5)));
        }

    },

    // 회원 탈퇴
    WITHDRAWAL_EMAIL {
        @Override
        public Bandwidth getLimit() {
            return Bandwidth.classic(2147483647, Refill.intervally(2147483647, Duration.ofMinutes(5)));
        }

    },

    // 비밀번호 변경 인증 코드번호 일치 확인
    VALIDATE_CODE {
        @Override
        public Bandwidth getLimit() {
            return Bandwidth.classic(2147483647, Refill.intervally(2147483647, Duration.ofMinutes(5)));
        }
    },

    // 회원 탈퇴 코드번호 일치 확인
    WITHDRAWAL_CODE {
        @Override
        public Bandwidth getLimit() {
            return Bandwidth.classic(2147483647, Refill.intervally(2147483647, Duration.ofMinutes(5)));
        }
    },

    // 리더 비밀번호 변경
    LEADER_CHANGE_PW {
        @Override
        public Bandwidth getLimit() {
            return Bandwidth.classic(2147483647, Refill.intervally(2147483647, Duration.ofMinutes(1)));
        }
    },

    // 이벤트 코드 검증
    EVENT_VERIFY {
        @Override
        public Bandwidth getLimit() {
            return Bandwidth.classic(2147483647, Refill.intervally(2147483647, Duration.ofMinutes(5)));
        }
    };

    public abstract Bandwidth getLimit();

}
