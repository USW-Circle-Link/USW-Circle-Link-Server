package com.USWCicrcleLink.server.aplict.api;

import com.USWCicrcleLink.server.aplict.service.AplictService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class AplictControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AplictService aplictService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("지원 가능 여부 확인 API 성공")
    void canApply_success() throws Exception {
        UUID clubUUID = UUID.randomUUID();
        doNothing().when(aplictService).checkIfCanApply(clubUUID);

        mockMvc.perform(get("/apply/can-apply/{clubUUID}", clubUUID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("지원 가능"));
    }

    @Test
    @DisplayName("구글 폼 URL 조회 API 성공")
    void getGoogleFormUrl_success() throws Exception {
        UUID clubUUID = UUID.randomUUID();
        when(aplictService.getGoogleFormUrlByClubUUID(clubUUID)).thenReturn("https://forms.gle/test-form-url");

        mockMvc.perform(get("/apply/{clubUUID}", clubUUID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("구글 폼 URL 조회 성공"))
                .andExpect(jsonPath("$.data").value("https://forms.gle/test-form-url"));
    }

    @Test
    @DisplayName("지원서 제출 API 성공")
    void submitAplict_success() throws Exception {
        UUID clubUUID = UUID.randomUUID();
        doNothing().when(aplictService).submitAplict(clubUUID);

        mockMvc.perform(post("/apply/{clubUUID}", clubUUID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("지원서 제출 성공"));
    }
}