package com.USWCicrcleLink.server.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {

    private String userAccount;
    private String userPw;
    private String email;

    //profile 관련 필드
    private Long userTempId;
    private String tempAccount;
    private String tempPw;
    private String tempName;
    private String tempHp;
    private String tempMajor;
    private String tempStudentNumber;
    private String tempEmail;
    private boolean EmailVerified;

}
