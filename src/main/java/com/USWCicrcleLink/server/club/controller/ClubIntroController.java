package com.USWCicrcleLink.server.club.controller;

import com.USWCicrcleLink.server.club.domain.RecruitmentStatus;
import com.USWCicrcleLink.server.club.dto.ClubIntroResponse;
import com.USWCicrcleLink.server.club.service.ClubIntroService;
import com.USWCicrcleLink.server.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/clubs")
public class ClubIntroController {

    private final ClubIntroService clubIntroService;

    //동아리 소개글 조회
    @GetMapping("/{clubId}/clubIntro")
    public ResponseEntity<ApiResponse> getClubIntroByClubId(@PathVariable("clubId") Long id) {
        ClubIntroResponse clubIntroResponse = clubIntroService.getClubIntroByClubId(id);
        ApiResponse response = new ApiResponse("동아리 소개글 조회 성공", clubIntroResponse);
        return ResponseEntity.ok(response);
    }

    //지원서 작성 페이지로 이동
    @GetMapping("/{clubId}/apply")
    public ResponseEntity<ApiResponse> showApplyPage(@PathVariable("clubId") Long id) {
        ClubIntroResponse clubIntroResponse = clubIntroService.getClubIntroByClubId(id);
        if (clubIntroResponse.getRecruitmentStatus().equals(RecruitmentStatus.CLOSED)) {
            return new ResponseEntity<>(new ApiResponse("모집이 마감되었습니다."), HttpStatus.FORBIDDEN);
        }
        ApiResponse response = new ApiResponse("지원 페이지 이동 성공", clubIntroResponse.getGoogleFormUrl());
        return ResponseEntity.ok(response);
    }

    //구글 폼으로 이동
    @GetMapping("/{clubId}/apply/form")
    public ResponseEntity<Void> applyToClub(@PathVariable("clubId") Long id) {
        ClubIntroResponse clubIntroResponse = clubIntroService.getClubIntroByClubId(id);
        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", clubIntroResponse.getGoogleFormUrl())
                .build();
    }
}