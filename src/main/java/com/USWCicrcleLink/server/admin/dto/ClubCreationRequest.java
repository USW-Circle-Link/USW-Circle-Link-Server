package com.USWCicrcleLink.server.admin.dto;

import com.USWCicrcleLink.server.club.domain.Department;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClubCreationRequest {
    private String leaderAccount;
    private String leaderPw;
    private String leaderPwConfirm;
    private String clubName;
    private Department department;
    private String adminPw;
}