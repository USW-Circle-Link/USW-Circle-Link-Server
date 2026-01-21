package com.USWCicrcleLink.server.clubLeader.dto.clubMembers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class ClubMembersAddFromExcelRequestList {

    @NotEmpty(message = "회원 목록은 비어있을 수 없습니다.")
    @Valid
    private List<ClubMembersAddFromExcelRequest> clubMembersAddFromExcelRequestList;

}
