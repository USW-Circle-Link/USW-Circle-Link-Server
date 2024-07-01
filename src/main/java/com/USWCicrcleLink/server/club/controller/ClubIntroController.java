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
    @GetMapping("/{id}/clubIntro")
    public ResponseEntity<ApiResponse> getClubIntroByClubId(@PathVariable("id") Long id) {
        ClubIntro clubIntro = clubIntroService.getClubIntroByClubId(id);
        ApiResponse response = new ApiResponse("동아리 소개글 조회 성공", clubIntro);
        return ResponseEntity.ok(response);
    }

    // 동아리 지원
    @PostMapping("/{id}/apply")
    public ResponseEntity<ApiResponse> applyToClub(@PathVariable("id") Long id) {
        clubIntroService.applyToClub(id);
        ApiResponse response = new ApiResponse("지원이 완료되었습니다.");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
