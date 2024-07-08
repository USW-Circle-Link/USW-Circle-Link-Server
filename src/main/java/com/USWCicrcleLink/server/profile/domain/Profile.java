package com.USWCicrcleLink.server.profile.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "PROFILE_TABLE")
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long profileId;

    private Long userId;

    private String userName;

    private String studentNumber;

    private String userHp;

    private String major;

    private LocalDateTime profileCreatedAt;

    private LocalDateTime profileUpdatedAt;

}
