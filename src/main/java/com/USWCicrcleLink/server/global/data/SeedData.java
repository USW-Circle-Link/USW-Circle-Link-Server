package com.USWCicrcleLink.server.global.data;

import com.USWCicrcleLink.server.admin.domain.Admin;
import com.USWCicrcleLink.server.admin.repository.AdminRepository;
import com.USWCicrcleLink.server.global.security.jwt.domain.Role;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Transactional
@RequiredArgsConstructor
@org.springframework.context.annotation.Profile("prod")
public class SeedData {
    private final PasswordEncoder passwordEncoder;
    private final com.USWCicrcleLink.server.user.repository.UserRepository userRepository;
    private final AdminRepository adminRepository;

    @PostConstruct
    public void init() {
        if (adminRepository.findTop1ByOrderByAdminIdAsc() == null) {
            initAdmin();
        }
    }

    // 관리자 동연회 데이터
    public void initAdmin() {
        UUID clubUnionUUID = UUID.randomUUID();
        // 동아리 연합회 관리자 계정
        Admin clubUnion = Admin.builder()
                .adminUUID(clubUnionUUID)
                .adminAccount("clubUnion")
                .adminPw(passwordEncoder.encode("hpsEetcTf7ymgy6")) // 비밀번호 암호화
                .adminName("동아리 연합회")
                .role(Role.ADMIN)
                .build();

        UUID developerUUID = UUID.randomUUID();
        // 개발자 계정
        Admin developer = Admin.builder()
                .adminUUID(developerUUID)
                .adminAccount("developer")
                .adminPw(passwordEncoder.encode("5MYcg7Cuvrh50fS")) // 비밀번호 암호화
                .adminName("운영자")
                .role(Role.ADMIN)
                .build();

        // 데이터 저장
        adminRepository.save(clubUnion);
        adminRepository.save(developer);

        // User 테이블 동기화
        userRepository.save(com.USWCicrcleLink.server.user.domain.User.builder()
                .userUUID(clubUnionUUID)
                .userAccount("clubUnion")
                .userPw(clubUnion.getAdminPw())
                .email("admin@club.union")
                .role(Role.ADMIN)
                .build());

        userRepository.save(com.USWCicrcleLink.server.user.domain.User.builder()
                .userUUID(developerUUID)
                .userAccount("developer")
                .userPw(developer.getAdminPw())
                .email("developer@club.union")
                .role(Role.ADMIN)
                .build());
    }
}
