package com.USWCicrcleLink.server.admin.admin.dto;

import com.USWCicrcleLink.server.club.club.domain.FloorPhotoEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminFloorPhotoCreationResponse {
    private FloorPhotoEnum floor;
    private String presignedUrl;
}
