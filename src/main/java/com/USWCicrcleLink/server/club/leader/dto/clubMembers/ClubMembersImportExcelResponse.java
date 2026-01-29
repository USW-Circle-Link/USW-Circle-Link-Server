package com.USWCicrcleLink.server.club.leader.dto.clubMembers;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClubMembersImportExcelResponse {

    private List<ExcelProfileMemberResponse> addClubMembers;

    private List<ExcelProfileMemberResponse> duplicateClubMembers;

}

