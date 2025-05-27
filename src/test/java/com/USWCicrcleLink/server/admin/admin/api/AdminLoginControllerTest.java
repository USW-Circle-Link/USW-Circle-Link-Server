package com.USWCicrcleLink.server.admin.admin.api;

import com.USWCicrcleLink.server.admin.admin.dto.AdminLoginRequest;
import com.USWCicrcleLink.server.admin.admin.dto.AdminLoginResponse;
import com.USWCicrcleLink.server.admin.admin.service.AdminLoginService;
import com.USWCicrcleLink.server.global.security.jwt.domain.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class AdminLoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminLoginService adminLoginService;

    @Test
    @DisplayName("운영팀 로그인 성공")
    void adminLogin_success() throws Exception {
        AdminLoginRequest request = new AdminLoginRequest("admin", "password123");
        AdminLoginResponse response = AdminLoginResponse.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .role(Role.ADMIN)
                .build();

        when(adminLoginService.adminLogin(any(AdminLoginRequest.class), any(HttpServletResponse.class)))
                .thenReturn(response);

        mockMvc.perform(post("/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("운영팀 로그인 성공")))
                .andExpect(jsonPath("$.data.accessToken", is("access-token")))
                .andExpect(jsonPath("$.data.refreshToken", is("refresh-token")));
    }
}