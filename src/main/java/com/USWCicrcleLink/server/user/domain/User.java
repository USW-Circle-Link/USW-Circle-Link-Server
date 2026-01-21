package com.USWCicrcleLink.server.user.domain;

import com.USWCicrcleLink.server.global.bucket4j.ClientIdentifier;
import com.USWCicrcleLink.server.global.security.jwt.domain.Role;
import com.USWCicrcleLink.server.user.dto.SignUpRequest;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "USER_TABLE")
public class User implements ClientIdentifier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "uuid", unique = true, nullable = false)
    private UUID userUUID;

    @Column(name = "user_account", unique = true, nullable = false,length=20)
    private String userAccount;

    @Column(name = "user_pw", nullable = false)
    private String userPw;

    @Column(unique = true, nullable = false,length = 30)
    private String email;

    @Column(name = "user_created_at", nullable = false)
    private LocalDateTime userCreatedAt;

    @Column(name = "user_updated_at", nullable = false)
    private LocalDateTime userUpdatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "role",nullable = false)
    private Role role;


    public static User createUser(SignUpRequest request,String encodedPw,String email){
        return User.builder()
                .userAccount(request.getAccount())
                .userPw(encodedPw)
                .email(email)
                .role(Role.USER)
                .build();
    }

    @PrePersist
    public void prePersist() {
        if (this.userUUID == null) {
            this.userUUID = UUID.randomUUID();  // 자동 UUID 생성
        }
        this.userCreatedAt = LocalDateTime.now();
        this.userUpdatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.userUpdatedAt = LocalDateTime.now();
    }

    public void updateUserPw(String userPw){
        this.userPw = userPw;
    }

    @Override
    public String getClientId() {
        return this.email;
    }
}

