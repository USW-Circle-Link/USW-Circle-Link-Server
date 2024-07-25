package com.USWCicrcleLink.server.admin.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AdminLoginRequest {
    private String adminAccount;
    private String adminPw;
}
