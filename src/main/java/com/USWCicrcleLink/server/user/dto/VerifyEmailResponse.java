package com.USWCicrcleLink.server.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VerifyEmailResponse {

    private UUID emailTokenId;
    private String account;

}