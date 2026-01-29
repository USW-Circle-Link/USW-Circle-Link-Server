package com.USWCicrcleLink.server.club.floor.dto;

import com.USWCicrcleLink.server.club.domain.FloorPhotoEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class FloorMapResponse {
    private FloorPhotoEnum floor;
    private String presignedUrl;
}

