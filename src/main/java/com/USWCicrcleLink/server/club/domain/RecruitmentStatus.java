package com.USWCicrcleLink.server.club.domain;

public enum RecruitmentStatus {
    OPEN,
    CLOSE;

    public boolean isOpen() {
        return this == OPEN;
    }
}
