package com.USWCicrcleLink.server.club.club.domain;

public enum RecruitmentStatus {
    OPEN,
    CLOSE;
    public RecruitmentStatus toggle() {
        return this == OPEN ? CLOSE : OPEN;

    }
}