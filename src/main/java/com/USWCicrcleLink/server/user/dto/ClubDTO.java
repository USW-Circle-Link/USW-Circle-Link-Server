package com.USWCicrcleLink.server.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

// 회원이 선택한 동아리 id
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ClubDTO {
    @NotNull
    private UUID clubUUID;
}
