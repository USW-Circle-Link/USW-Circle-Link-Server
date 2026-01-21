package com.USWCicrcleLink.server.global.data;

import com.USWCicrcleLink.server.admin.admin.domain.Admin;
import com.USWCicrcleLink.server.admin.admin.repository.AdminRepository;
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
    private final AdminRepository adminRepository;


    @PostConstruct
    public void init() {
        if (adminRepository.findTop1ByOrderByAdminIdAsc()==null){
        initAdmin();
        }
    }

    //관리자 동연회 데이터
    public void initAdmin() {
        // 동아리 연합회 관리자 계정
        Admin clubUnion = Admin.builder()
                .adminUUID(UUID.randomUUID())
                .adminAccount("clubUnion")
                .adminPw(passwordEncoder.encode("hpsEetcTf7ymgy6"))  // 비밀번호 암호화
                .adminName("동아리 연합회")
                .role(Role.ADMIN)
                .build();

        // 개발자 계정
        Admin developer = Admin.builder()
                .adminUUID(UUID.randomUUID())
                .adminAccount("developer")
                .adminPw(passwordEncoder.encode("5MYcg7Cuvrh50fS"))  // 비밀번호 암호화
                .adminName("운영자")
                .role(Role.ADMIN)
                .build();

        // 데이터 저장
        adminRepository.save(clubUnion);
        adminRepository.save(developer);
    }
}
