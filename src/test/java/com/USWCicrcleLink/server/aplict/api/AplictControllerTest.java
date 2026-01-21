//package com.USWCicrcleLink.server.aplict.api;
//
//import com.USWCicrcleLink.server.aplict.dto.AplictRequest;
//import com.USWCicrcleLink.server.aplict.service.AplictService;
//import com.USWCicrcleLink.server.global.security.jwt.JwtProvider;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.mock.web.MockHttpServletResponse;
//import org.springframework.security.test.context.support.WithMockUser;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
//import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyLong;
//import static org.mockito.Mockito.doNothing;
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
//
//@SpringBootTest
//@AutoConfigureMockMvc
//public class AplictControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockBean
//    private AplictService aplictService;
//
//    @Test
//    @DisplayName("지원 가능한지 확인")
//    @WithMockUser(username = "testUser", roles = {"USER"})
//    void testCanApply() throws Exception {
//        doNothing().when(aplictService).checkIfCanApply(anyLong());
//
//        mockMvc.perform(MockMvcRequestBuilders.get("/apply/can-apply/1"))
//                .andExpect(MockMvcResultMatchers.status().isOk())
//                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("지원 가능"))
//                .andDo(print());
//    }
//
//    @Test
//    @DisplayName("구글 폼 url 조회")
//    @WithMockUser(username = "testUser", roles = {"USER"})
//    void testGetGoogleFormUrl() throws Exception {
//        String googleFormUrl = "https://example.com/google-form";
//        when(aplictService.getGoogleFormUrlByClubId(anyLong())).thenReturn(googleFormUrl);
//
//        mockMvc.perform(MockMvcRequestBuilders.get("/apply/1"))
//                .andExpect(MockMvcResultMatchers.status().isOk())
//                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("구글 폼 URL 조회 성공"))
//                .andExpect(MockMvcResultMatchers.jsonPath("$.data").value(googleFormUrl))
//                .andDo(print());
//    }
//
//    @Test
//    @DisplayName("지원 완료")
//    @WithMockUser(username = "testUser", roles = {"USER"})
//    void testSubmitAplict() throws Exception {
//        doNothing().when(aplictService).submitAplict(anyLong(), any(AplictRequest.class));
//
//        mockMvc.perform(MockMvcRequestBuilders.post("/apply/1")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("{\"aplictGoogleFormUrl\":\"https://example.com/google-form\"}"))
//                .andExpect(MockMvcResultMatchers.status().isOk())
//                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("지원서 제출 성공"))
//                .andDo(print());
//    }
//}
