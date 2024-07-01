package com.USWCicrcleLink.server.aplict.controller;

import com.USWCicrcleLink.server.aplict.dto.AplictRequest;
import com.USWCicrcleLink.server.aplict.service.AplictService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AplictControllerTest {

    @InjectMocks
    private AplictController aplictController;

    @Mock
    private AplictService aplictService;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(aplictController).build();
    }

    @Test
    public void 지원서제출() throws Exception {
        AplictRequest request = new AplictRequest();
        request.setProfileId(1L);
        request.setClubId(1L);
        request.setAplictGoogleFormUrl("http://googleform.url");

        mockMvc.perform(post("/aplict/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"profileId\": 1, \"clubId\": 1, \"aplictGoogleFormUrl\": \"http://googleform.url\" }"))
                .andExpect(status().isOk());
    }

    @Test
    public void 지원서조회() throws Exception {
        when(aplictService.getAplictByClubId(any(Long.class))).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/aplict/club/1"))
                .andExpect(status().isOk());
    }
}