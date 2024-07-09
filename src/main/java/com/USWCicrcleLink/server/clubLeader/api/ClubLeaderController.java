package com.USWCicrcleLink.server.clubLeader.api;

import com.USWCicrcleLink.server.club.domain.RecruitmentStatus;
import com.USWCicrcleLink.server.club.service.ClubLeaderService;
import com.USWCicrcleLink.server.clubLeader.dto.ClubInfoRequest;
import com.USWCicrcleLink.server.clubLeader.dto.ClubIntroRequest;
import com.USWCicrcleLink.server.clubLeader.dto.RecruitmentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/club-leader")
public class ClubLeaderController {

    private ClubLeaderService clubLeaderService;

    @PatchMapping("/info")
    public ResponseEntity<Boolean> updateClubInfo(@Validated ClubInfoRequest clubInfoRequest) throws IOException {
        clubLeaderService.updateClubInfo(clubInfoRequest);
        return new ResponseEntity<>(true, HttpStatus.OK);
    }

    @PatchMapping("/intro")
    public ResponseEntity<Boolean> setClubInfo(@Validated ClubIntroRequest clubInfoRequest) throws IOException {
        clubLeaderService.updateClubIntro(clubInfoRequest);
        return new ResponseEntity<>(true, HttpStatus.OK);
    }

    @PatchMapping("/toggle-recruitment")
    public ResponseEntity<RecruitmentStatus> setClubInfo(@Validated RecruitmentRequest recruitmentRequest) {
        // 원래는 GET 요청임 토큰때문
        return new ResponseEntity<>(clubLeaderService.toggleRecruitmentStatus(recruitmentRequest), HttpStatus.OK);
    }
}