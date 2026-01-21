package com.USWCicrcleLink.server.admin.admin.domain;

import com.USWCicrcleLink.server.global.security.jwt.domain.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "ADMIN_TABLE")
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admin_id")
    private Long adminId;

    @Builder.Default
    @Column(name = "admin_UUID", nullable = false, unique = true, updatable = false)
    private UUID adminUUID = UUID.randomUUID();

    @Column(name = "admin_account", nullable = false, unique = true, length = 20)
    private String adminAccount;

    @Column(name = "admin_pw", nullable = false)
    private String adminPw;

    @Column(name = "admin_name", nullable = false, length = 30)
    private String adminName;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 10)
    private Role role;

    @PrePersist
    public void generateUUID() {
        if (this.adminUUID == null) {
            this.adminUUID = UUID.randomUUID();
        }
    }
}