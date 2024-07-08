package com.USWCicrcleLink.server.user.dto;

import lombok.Data;

@Data
public class UpdatePwRequest {
    private String newPassword;
    private String confirmNewPassword;
}
