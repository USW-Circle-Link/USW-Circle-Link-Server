package com.USWCicrcleLink.server.aplict.service;

import com.USWCicrcleLink.server.aplict.domain.Aplict;
import com.USWCicrcleLink.server.aplict.dto.AplictRequest;
import com.USWCicrcleLink.server.aplict.dto.AplictResponse;
import com.USWCicrcleLink.server.aplict.repository.AplictRepository;
import com.USWCicrcleLink.server.club.club.domain.Club;
import com.USWCicrcleLink.server.club.clubIntro.domain.ClubIntro;
import com.USWCicrcleLink.server.club.clubIntro.repository.ClubIntroRepository;
import com.USWCicrcleLink.server.club.club.repository.ClubRepository;
import com.USWCicrcleLink.server.profile.domain.Profile;
import com.USWCicrcleLink.server.profile.repository.ProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AplictServiceTest {

    @InjectMocks
    private AplictService aplictService;

    @Mock
    private AplictRepository aplictRepository;

    @Mock
    private ClubRepository clubRepository;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private ClubIntroRepository clubIntroRepository;

    private Club club;
    private Profile profile;
    private ClubIntro clubIntro;

    @BeforeEach
    void setUp() {
        //given
        club = Club.builder()
                .clubId(1L)
                .clubName("Test Club")
                .build();

        profile = Profile.builder()
                .profileId(1L)
                .build();

        clubIntro = ClubIntro.builder()
                .club(club)
                .googleFormUrl("https://forms.gle/testForm")
                .build();
    }

    @Test
    void 지원서작성_구글폼URL조회_성공() {
        //given
        when(clubIntroRepository.findByClubClubId(anyLong())).thenReturn(Optional.of(clubIntro));

        //when
        String googleFormUrl = aplictService.getGoogleFormUrlByClubId(club.getClubId());

        //then
        assertEquals("https://forms.gle/testForm", googleFormUrl);
    }

    @Test
    void 지원서작성_구글폼URL조회_실패_동아리소개없음() {
        //given
        when(clubIntroRepository.findByClubClubId(anyLong())).thenReturn(Optional.empty());

        //when
        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () ->
                aplictService.getGoogleFormUrlByClubId(club.getClubId()));
        //then
        assertEquals("해당 동아리에 대한 소개를 찾을 수 없습니다.", exception.getMessage());
    }

    @Test
    void 동아리지원서제출_성공() {
        //given
        UUID userUUID = UUID.randomUUID();
        AplictRequest request = AplictRequest.builder()
                .aplictGoogleFormUrl("https://forms.gle/testForm")
                .build();

        when(profileRepository.findByUser_UserUUID(any(UUID.class))).thenReturn(Optional.of(profile));
        when(clubRepository.findById(anyLong())).thenReturn(Optional.of(club));
        when(aplictRepository.save(any(Aplict.class))).thenAnswer(invocation -> invocation.getArgument(0));

        //when
        AplictResponse response = aplictService.submitAplict(userUUID, club.getClubId(), request);

        //then
        assertNotNull(response);
        assertEquals(request.getAplictGoogleFormUrl(), response.getAplictGoogleFormUrl());
        verify(aplictRepository, times(1)).save(any(Aplict.class));
    }

    @Test
    void 동아리지원서제출_실패_사용자없음() {
        //given
        UUID userUUID = UUID.randomUUID();
        AplictRequest request = AplictRequest.builder()
                .aplictGoogleFormUrl("https://forms.gle/testForm")
                .build();

        when(profileRepository.findByUser_UserUUID(any(UUID.class))).thenReturn(Optional.empty());

        //when
        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () ->
                aplictService.submitAplict(userUUID, club.getClubId(), request));
        //then
        assertEquals("사용자를 찾을 수 없습니다.", exception.getMessage());
    }

    @Test
    void 동아리지원서제출_실패_동아리없음() {
        //given
        UUID userUUID = UUID.randomUUID();
        AplictRequest request = AplictRequest.builder()
                .aplictGoogleFormUrl("https://forms.gle/testForm")
                .build();

        when(profileRepository.findByUser_UserUUID(any(UUID.class))).thenReturn(Optional.of(profile));
        when(clubRepository.findById(anyLong())).thenReturn(Optional.empty());

        //when
        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () ->
                aplictService.submitAplict(userUUID, club.getClubId(), request));
        //then
        assertEquals("동아리를 찾을 수 없습니다.", exception.getMessage());
    }
}
