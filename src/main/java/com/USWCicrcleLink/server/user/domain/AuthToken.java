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
@Table(name = "AUTHTOKEN_TABLE")
public class AuthToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AUTHTOKEN_ID")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_uuid", referencedColumnName = "uuid", unique = true)
    private User user;

    private String authCode;

    public static AuthToken createAuthToken(User user, String authCode) {
        return AuthToken.builder()
                .user(user)
                .authCode(authCode)
                .build();
    }

    public boolean isAuthCodeValid(String authCode) {
        return this.authCode.equals(authCode);
    }
}