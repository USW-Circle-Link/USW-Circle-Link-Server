package com.USWCicrcleLink.server.user.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignUpuuidResponse {

    @NotNull
    private UUID emailTokenUUID;
    @NotNull
    private UUID signupUUID;
}
