package com.USWCicrcleLink.server.clubLeaders.domain;

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
@Table(name = "LEADER_TABLE")
public class Leader {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long leaderId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clubId")
    private Club club;

    private long clubMemberId;

    private long leaderAccount;

    private long leaderPw;

}
