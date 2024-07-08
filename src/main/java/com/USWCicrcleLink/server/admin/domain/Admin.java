package com.USWCicrcleLink.server.admin.domain;

import com.USWCicrcleLink.server.notice.domain.Notice;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "ADMIN_TABLE")
public class Admin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long adminId;

    private String adminAccount;

    private String adminPw;

    private String adminNickname;

    @OneToMany(mappedBy = "admin", cascade = CascadeType.ALL)
    private List<Notice> notices = new ArrayList<>();
}
