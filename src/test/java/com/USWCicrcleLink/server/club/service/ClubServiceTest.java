package com.USWCicrcleLink.server.club.service;

import com.USWCicrcleLink.server.club.domain.Club;
import com.USWCicrcleLink.server.club.domain.Department;
import com.USWCicrcleLink.server.club.repository.ClubRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClubServiceTest {

    @Mock
    private ClubRepository clubRepository;

    @InjectMocks
    private ClubService clubService;

    private Club club;

    @BeforeEach
    void setUp() {
        club = new Club();
        club.setClubId(1L);
        club.setClubName("Test Club");
        club.setDepartment(Department.ACADEMIC);
        club.setChatRoomUrl("http://testchatroom.com");
    }

    @Test
    void 모든클럽조회() {
        when(clubRepository.findAll()).thenReturn(Collections.singletonList(club));

        List<Club> clubs = clubService.getAllClubs();
        assertNotNull(clubs);
        assertEquals(1, clubs.size());
        assertEquals(club.getClubName(), clubs.get(0).getClubName());
    }

    @Test
    void 클럽조회() {
        when(clubRepository.findById(1L)).thenReturn(Optional.of(club));

        Club foundClub = clubService.getClubById(1L);
        assertNotNull(foundClub);
        assertEquals(club.getClubName(), foundClub.getClubName());
    }

    @Test
    void 분과별클럽조회() {
        when(clubRepository.findByDepartment(Department.ACADEMIC)).thenReturn(Collections.singletonList(club));

        List<Club> clubs = clubService.getClubsByDepartment(Department.ACADEMIC);
        assertNotNull(clubs);
        assertEquals(1, clubs.size());
        assertEquals(club.getDepartment(), clubs.get(0).getDepartment());
    }
}