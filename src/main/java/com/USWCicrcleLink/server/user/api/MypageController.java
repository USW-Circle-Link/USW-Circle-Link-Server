package com.USWCicrcleLink.server.user.api;

import com.USWCicrcleLink.server.global.response.ApiResponse;
import com.USWCicrcleLink.server.user.dto.ClubFloorPhotoResponse;
import com.USWCicrcleLink.server.user.dto.MyAplictResponse;
import com.USWCicrcleLink.server.user.dto.MyClubResponse;
import com.USWCicrcleLink.server.user.service.MypageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/mypages")
@RequiredArgsConstructor
public class MypageController {

    private final MypageService mypageService;

    //소속된 동아리 조회
    @GetMapping("/my-clubs")
    public ApiResponse<List<MyClubResponse>>getMyClubById(){
        List<MyClubResponse> myclubs = mypageService.getMyClubById();
        return new ApiResponse<>("소속된 동아리 목록 조회 성공", myclubs);
    }

    //지원한 동아리 조회
    @GetMapping("/aplict-clubs")
    public ApiResponse<List<MyAplictResponse>> getAplictClubById(){
        List<MyAplictResponse> aplictClubs = mypageService.getAplictClubById();
        return new ApiResponse<>("지원한 동아리 목록 조회 성공", aplictClubs);
    }

    //동아리방 층별 사진 조회
    @GetMapping("/clubs/{floor}/photo")
    public ApiResponse<ClubFloorPhotoResponse>getClubFloorPhoto(@PathVariable("floor") String floor){
        ClubFloorPhotoResponse clubFloorPhotoResponse = mypageService.getClubFloorPhoto(floor);
        return new ApiResponse<>("동아리방 층별 사진 조회 성공",clubFloorPhotoResponse);
    }
}