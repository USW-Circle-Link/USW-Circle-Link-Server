package com.USWCicrcleLink.server.aplict.api;

import com.USWCicrcleLink.server.aplict.dto.AplictRequest;
import com.USWCicrcleLink.server.aplict.service.AplictService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(AplictController.class)
public class AplictControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AplictService aplictService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void 지원서작성_구글폼URL조회_성공() throws Exception {
        //given
        String googleFormUrl = "https://forms.gle/testForm";
        Mockito.when(aplictService.getGoogleFormUrlByClubId(anyLong())).thenReturn(googleFormUrl);

        //when
        mockMvc.perform(get("/aplict/{clubId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("구글 폼 URL 조회 성공")))
                .andExpect(jsonPath("$.data", is(googleFormUrl)));
    }

    @Test
    void 동아리지원서제출_성공() throws Exception {
        //given
        UUID userUUID = UUID.randomUUID();
        AplictRequest request = new AplictRequest();
        //when
        mockMvc.perform(post("/aplict/submit/{clubId}", 1L)
                        .header("User-uuid", userUUID.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("지원서 제출 성공")));
    }
}
