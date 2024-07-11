package com.USWCicrcleLink.server.user.api;

import com.USWCicrcleLink.server.global.response.ApiResponse;
import com.USWCicrcleLink.server.user.dto.MyPageResponse;
import com.USWCicrcleLink.server.user.service.MypageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/mypage")
@RequiredArgsConstructor
public class MypageController {

    private final MypageService mypageService;

    //소속된 동아리 조회
    @GetMapping("/MyClub")
    public ApiResponse<List<MyPageResponse>> getMyClubByUUID(@RequestParam UUID uuid){
        List<MyPageResponse> myClubs = mypageService.getMyClubsByUUID(uuid);
        return new ApiResponse<>("소속된 동아리 목록 조회 성공", myClubs);
    }

    @GetMapping("/aplictClub")
    public ApiResponse<List<MyPageResponse>> getAplictClubByUUID(@RequestParam UUID uuid){
        List<MyPageResponse> aplictClubs = mypageService.getAplictClubByUUID(uuid);
        return new ApiResponse<>("지원한 동아리 목록 조회 성공", aplictClubs);
    }

}