package com.USWCicrcleLink.server.user.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "USERTEMP_TABLE")
public class UserTemp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userTempId;

    private Long emailTokenId;

    private String tempAccount;

    private String tempPw;

    private String tempName;

    private String tempStudentNumber;

    private String tempHp;

    private String tempMajor;

    private String tempEmail;

    private boolean isEmailVerified;

}
