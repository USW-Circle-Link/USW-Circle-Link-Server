package com.USWCicrcleLink.server.club.application.dto;

import com.USWCicrcleLink.server.club.application.domain.AplictStatus;
import com.USWCicrcleLink.server.global.validation.ValidationGroups;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicantResultsRequest {

    @NotNull(message = "지원서는 필수 입력값입니다.", groups = ValidationGroups.NotBlankGroup.class)
    private UUID aplictUUID;

}
