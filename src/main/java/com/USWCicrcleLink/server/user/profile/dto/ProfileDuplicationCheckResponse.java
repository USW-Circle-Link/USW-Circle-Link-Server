package com.USWCicrcleLink.server.user.profile.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileDuplicationCheckResponse {
    private boolean exists;
    // NOT_FOUND, NO_CLUB, SAME_CLUB, OTHER_CLUB
    private String classification;
    private boolean inTargetClub;
    private List<UUID> clubuuids;
    private UUID targetClubuuid;
    private Long profileId;
}
