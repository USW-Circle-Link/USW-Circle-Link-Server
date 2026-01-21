//package com.USWCicrcleLink.server.profile.api;
//
//import com.USWCicrcleLink.server.profile.dto.ProfileRequest;
//import com.USWCicrcleLink.server.profile.dto.ProfileResponse;
//import com.USWCicrcleLink.server.profile.service.ProfileService;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mockito;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.util.UUID;
//
//import static org.hamcrest.Matchers.is;
//import static org.mockito.ArgumentMatchers.any;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@ExtendWith(SpringExtension.class)
//@WebMvcTest(ProfileController.class)
//class ProfileControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockBean
//    private ProfileService profileService;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @Test
//    @DisplayName("PATCH 프로필 수정 컨트롤러 로직 확인")
//    void updateProfile_성공() throws Exception {
//        //given
//        UUID userUUID = UUID.randomUUID();
//        ProfileRequest request = new ProfileRequest();
//        ProfileResponse response = new ProfileResponse();
//
//        Mockito.when(profileService.updateProfile(any(UUID.class), any(ProfileRequest.class))).thenReturn(response);
//
//        //when
//        mockMvc.perform(patch("/profiles/{uuid}", userUUID)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                //then
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.message", is("프로필 수정 성공")))
//                .andExpect(jsonPath("$.data.userName").value(is(response.getUserName())))
//                .andExpect(jsonPath("$.data.studentNumber").value(is(response.getStudentNumber())))
//                .andExpect(jsonPath("$.data.userHp").value(is(response.getUserHp())))
//                .andExpect(jsonPath("$.data.major").value(is(response.getMajor())));
//    }
//}