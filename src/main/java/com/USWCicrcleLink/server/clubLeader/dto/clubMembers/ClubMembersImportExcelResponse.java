package com.USWCicrcleLink.server.clubLeader.dto.clubMembers;

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
