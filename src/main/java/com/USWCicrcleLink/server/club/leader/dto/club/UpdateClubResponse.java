package com.USWCicrcleLink.server.club.leader.dto.club;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateClubResponse {
    private String mainPhotoPresignedUrl;
    private List<String> infoPhotoPresignedUrls;
    private com.USWCicrcleLink.server.club.domain.RecruitmentStatus recruitmentStatus;
}
