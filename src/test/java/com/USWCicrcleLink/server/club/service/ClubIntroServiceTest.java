package com.USWCicrcleLink.server.club.service;

import com.USWCicrcleLink.server.club.domain.ClubIntro;
import com.USWCicrcleLink.server.club.repository.ClubIntroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@ExtendWith(MockitoExtension.class)
class ClubIntroServiceTest {

    @InjectMocks
    private ClubIntroService clubIntroService;

    @Mock
    private ClubIntroRepository clubIntroRepository;

    private ClubIntro clubIntro;

    @BeforeEach
    void setUp() {
        clubIntro = ClubIntro.builder()
                .clubIntroId(1L)
                .clubIntro("This is an intro")
                .additionalPhotoPath1("intro.jpg")
                .build();
    }

    @Test
    void 클럽소개글조회() {
        when(clubIntroRepository.findByClubClubId(1L)).thenReturn(Optional.ofNullable(clubIntro));

        ClubIntro foundClubIntro = clubIntroService.getClubIntroByClubId(1L);

        assertEquals(clubIntro, foundClubIntro);
        verify(clubIntroRepository, times(1)).findByClubClubId(1L);
    }

    @Test
    void 클럽이존재하지않을때소개글조회() {
        when(clubIntroRepository.findByClubClubId(1L)).thenReturn(null);

        ClubIntro foundClubIntro = clubIntroService.getClubIntroByClubId(1L);

        assertNull(foundClubIntro);
        verify(clubIntroRepository, times(1)).findByClubClubId(1L);
    }
}