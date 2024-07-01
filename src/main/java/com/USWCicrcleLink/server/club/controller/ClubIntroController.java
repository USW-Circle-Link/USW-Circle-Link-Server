package com.USWCicrcleLink.server.club.controller;

import com.USWCicrcleLink.server.club.domain.ClubIntro;
import com.USWCicrcleLink.server.club.service.ClubIntroService;
import com.USWCicrcleLink.server.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/clubs")
public class ClubIntroController {

    private final ClubIntroService clubIntroService;

    // 동아리 소개글 조회
    @GetMapping("/{clubId}/clubIntro")
    public ResponseEntity<ApiResponse> getClubIntroByClubId(@PathVariable("clubId") Long id) {
        ClubIntro clubIntro = clubIntroService.getClubIntroByClubId(id);
        ApiResponse response = new ApiResponse("동아리 소개글 조회 성공", clubIntro);
        return ResponseEntity.ok(response);
    }

    // 지원서 작성 페이지로 이동
    @GetMapping("/{clubId}/apply")
    public ResponseEntity<ApiResponse> showApplyPage(@PathVariable("clubId") Long id) {
        ClubIntro clubIntro = clubIntroService.getClubIntroByClubId(id);
        ApiResponse response = new ApiResponse("지원 페이지 이동 성공", clubIntro.getGoogleFormUrl());
        return ResponseEntity.ok(response);
    }

    // 구글 폼으로 이동
    @GetMapping("/{clubId}/apply/form")
    public ResponseEntity<Void> applyToClub(@PathVariable("clubId") Long id) {
        ClubIntro clubIntro = clubIntroService.getClubIntroByClubId(id);
        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", clubIntro.getGoogleFormUrl())
                .build();
    }
}
