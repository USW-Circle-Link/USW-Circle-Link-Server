package com.USWCicrcleLink.server.user.dto;

import lombok.Data;

@Data
public class UpdatePwRequest {
    private String userPw;
    private String newPw;
    private String confirmNewPw;
}
