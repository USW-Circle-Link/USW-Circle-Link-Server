package com.USWCicrcleLink.server.profile.domain;

import com.USWCicrcleLink.server.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

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

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "userId")
    private User user;
    @Setter
    private String userName;
    @Setter
    private String studentNumber;
    @Setter
    private String userHp;
    @Setter
    private String major;

    private LocalDateTime profileCreatedAt;
    @Setter
    private LocalDateTime profileUpdatedAt;

}
