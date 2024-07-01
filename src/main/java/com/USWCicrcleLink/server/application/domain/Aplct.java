package com.USWCicrcleLink.server.application.domain;

import com.USWCicrcleLink.server.clubLeaders.domain.Club;
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
@Table(name = "APPLICATION_TABLE")
public class Aplct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long aplctId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clubId")
    private Club club;

    private Long profileId;

    private String aplctText;

    @Enumerated(EnumType.STRING)
    private  AplctStatus status;

    private LocalDateTime aplctSubmittedAt;

}