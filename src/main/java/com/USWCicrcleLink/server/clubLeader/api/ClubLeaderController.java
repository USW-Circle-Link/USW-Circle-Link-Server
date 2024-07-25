package com.USWCicrcleLink.server.clubLeader.api;

import com.USWCicrcleLink.server.club.club.domain.RecruitmentStatus;
import com.USWCicrcleLink.server.clubLeader.dto.ClubInfoRequest;
import com.USWCicrcleLink.server.clubLeader.dto.ClubInfoResponse;
import com.USWCicrcleLink.server.clubLeader.dto.ClubIntroRequest;
import com.USWCicrcleLink.server.clubLeader.dto.LeaderToken;
import com.USWCicrcleLink.server.clubLeader.service.ClubLeaderService;
import com.USWCicrcleLink.server.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/club-leader")
public class ClubLeaderController {

    private final ClubLeaderService clubLeaderService;

    @GetMapping("/info")
    public ResponseEntity<ApiResponse> getClubInfo(LeaderToken token) {
        ApiResponse<ClubInfoResponse> clubInfo = clubLeaderService.getClubInfo(token);
        return new ResponseEntity<>(clubInfo, HttpStatus.OK);
    }

    @PatchMapping("/info")
    public ResponseEntity<Boolean> updateClubInfo(LeaderToken token, @Validated ClubInfoRequest clubInfoRequest) throws IOException {
        clubLeaderService.updateClubInfo(token, clubInfoRequest);
        return new ResponseEntity<>(true, HttpStatus.OK);
    }

    @PatchMapping("/intro")
    public ResponseEntity<Boolean> setClubInfo(LeaderToken token, @Validated ClubIntroRequest clubInfoRequest) throws IOException {
        clubLeaderService.updateClubIntro(token, clubInfoRequest);
        return new ResponseEntity<>(true, HttpStatus.OK);
    }

    @PatchMapping("/toggle-recruitment")
    public ResponseEntity<RecruitmentStatus> setClubInfo(LeaderToken token) {
        // 원래는 Patch 요청임 토큰때문
        return new ResponseEntity<>(clubLeaderService.toggleRecruitmentStatus(token), HttpStatus.OK);
    }

    @GetMapping("/members")
    public ResponseEntity<ApiResponse> getClubMembers(LeaderToken token) {
        // 원래는 GET 요청임 토큰때문
        return new ResponseEntity<>(clubLeaderService.findClubMembers(token), HttpStatus.OK);
    }

    @DeleteMapping("/members/{clubMemberId}")
    public ResponseEntity<ApiResponse> deleteClubMember(@PathVariable Long clubMemberId, LeaderToken token) {
        return new ResponseEntity<>(clubLeaderService.deleteClubMember(clubMemberId, token), HttpStatus.OK);
    }

}
