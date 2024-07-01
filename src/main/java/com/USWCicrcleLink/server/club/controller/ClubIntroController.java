package com.USWCicrcleLink.server.club.controller;

import com.USWCicrcleLink.server.club.domain.ClubIntro;
import com.USWCicrcleLink.server.club.service.ClubIntroService;
import com.USWCicrcleLink.server.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
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
    @GetMapping("/{id}/clubIntro")
    public ResponseEntity<ApiResponse> getClubIntroByClubId(@PathVariable("id") Long id) {
        ClubIntro clubIntro = clubIntroService.getClubIntroByClubId(id);
        ApiResponse response = new ApiResponse("동아리 소개글 조회 성공", clubIntro);
        return ResponseEntity.ok(response);
    }
}
