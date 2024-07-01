package com.USWCicrcleLink.server.aplict.domain;

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
public class Aplict {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long aplctId;

    private Long clubId;

    private Long profileId;

    private String aplctText;

    @Enumerated(EnumType.STRING)
    private AplictStatus status;

    private LocalDateTime aplctSubmittedAt;

}