package com.USWCicrcleLink.server.admin.admin.service;

import com.USWCicrcleLink.server.admin.admin.domain.Admin;
import com.USWCicrcleLink.server.admin.admin.dto.AdminLoginRequest;
import com.USWCicrcleLink.server.admin.admin.dto.AdminLoginResponse;
import com.USWCicrcleLink.server.admin.admin.repository.AdminRepository;
import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.UserException;
import com.USWCicrcleLink.server.global.security.jwt.JwtProvider;
import com.USWCicrcleLink.server.global.security.jwt.domain.Role;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminLoginServiceTest {

    @Mock
    private AdminRepository adminRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtProvider jwtProvider;
    @Mock
    private HttpServletResponse httpServletResponse;

    @InjectMocks
    private AdminLoginService adminLoginService;

    @Test
    @DisplayName("adminLogin - 로그인 성공")
    void testAdminLogin_success() {
        AdminLoginRequest request = new AdminLoginRequest("admin", "1234");
        UUID uuid = UUID.randomUUID();
        Admin admin = Admin.builder().adminAccount("admin").adminPw("hashed_pw").adminUUID(uuid).build();

        when(adminRepository.findByAdminAccount("admin")).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("1234", "hashed_pw")).thenReturn(true);
        when(jwtProvider.createAccessToken(uuid, httpServletResponse)).thenReturn("access-token");
        when(jwtProvider.createRefreshToken(uuid, httpServletResponse)).thenReturn("refresh-token");

        AdminLoginResponse response = adminLoginService.adminLogin(request, httpServletResponse);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    @DisplayName("adminLogin - 존재하지 않는 admin 예외")
    void testAdminLogin_notFound() {
        AdminLoginRequest request = new AdminLoginRequest("admin", "1234");
        when(adminRepository.findByAdminAccount("admin")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminLoginService.adminLogin(request, httpServletResponse))
                .isInstanceOf(UserException.class)
                .hasMessageContaining(ExceptionType.USER_AUTHENTICATION_FAILED.getMessage());
    }

    @Test
    @DisplayName("adminLogin - 비밀번호 불일치 예외")
    void testAdminLogin_passwordMismatch() {
        AdminLoginRequest request = new AdminLoginRequest("admin", "12345");
        Admin admin = Admin.builder().adminAccount("admin").adminPw("hashed_pw").adminUUID(UUID.randomUUID()).build();

        when(adminRepository.findByAdminAccount("admin")).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("12345", "hashed_pw")).thenReturn(false);

        assertThatThrownBy(() -> adminLoginService.adminLogin(request, httpServletResponse))
                .isInstanceOf(UserException.class)
                .hasMessageContaining(ExceptionType.USER_AUTHENTICATION_FAILED.getMessage());
    }
}
