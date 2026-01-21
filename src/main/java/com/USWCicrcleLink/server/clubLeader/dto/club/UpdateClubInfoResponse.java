package com.USWCicrcleLink.server.clubLeader.dto.club;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateClubInfoResponse {
    String presignedUrl;
}
