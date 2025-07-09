package com.USWCicrcleLink.server.global.security.details.service;

import com.USWCicrcleLink.server.admin.admin.domain.Admin;
import com.USWCicrcleLink.server.admin.admin.repository.AdminRepository;
import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.UserException;
import com.USWCicrcleLink.server.global.security.details.CustomAdminDetails;
import com.USWCicrcleLink.server.global.security.jwt.domain.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class CustomAdminDetailsServiceTest {

    @Mock
    AdminRepository adminRepository;

    @InjectMocks
    CustomAdminDetailsService customAdminDetailsService;

    @Nested
    @DisplayName("loadUserByUuid 메서드")
    class LoadUserByUuid {

        @Test
        @DisplayName("정상적으로 Admin이 조회되면 CustomAdminDetails를 반환")
        void loadUserByUuid_정상() {
            // given
            UUID adminUuid = UUID.randomUUID();
            Admin admin = Admin.builder()
                    .adminUUID(adminUuid)
                    .adminAccount("admin@company.com")
                    .adminPw("encoded_pw")
                    .role(Role.ADMIN)
                    .build();

            given(adminRepository.findByAdminUUID(adminUuid)).willReturn(Optional.of(admin));

            // when
            UserDetails result = customAdminDetailsService.loadUserByUuid(adminUuid);

            // then
            assertThat(result).isInstanceOf(CustomAdminDetails.class);

            CustomAdminDetails details = (CustomAdminDetails) result;
            assertThat(details.getAdminUUID()).isEqualTo(adminUuid);
            assertThat(details.getUsername()).isEqualTo(adminUuid.toString());
            assertThat(details.getPassword()).isEqualTo(admin.getAdminPw());
            assertThat(details.getAuthorities()).extracting("authority")
                    .containsExactly("ROLE_" + admin.getRole().name());
        }

        @Test
        @DisplayName("Admin이 존재하지 않으면 UserException이 발생")
        void loadUserByUuid_존재하지않음_예외() {
            // given
            UUID adminUuid = UUID.randomUUID();
            given(adminRepository.findByAdminUUID(adminUuid)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> customAdminDetailsService.loadUserByUuid(adminUuid))
                    .isInstanceOf(UserException.class)
                    .hasMessageContaining(ExceptionType.USER_NOT_EXISTS.getMessage());
        }
    }
}
