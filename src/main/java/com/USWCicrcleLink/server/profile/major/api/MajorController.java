package com.USWCicrcleLink.server.profile.major.api;

import com.USWCicrcleLink.server.global.response.ApiResponse;
import com.USWCicrcleLink.server.profile.major.dto.MajorListResponse;
import com.USWCicrcleLink.server.profile.major.service.MajorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/majors")
public class MajorController {
    private final MajorService majorService;

    @GetMapping
    public ApiResponse<List<MajorListResponse>> getAllMajors(){
        List<MajorListResponse> majorListResponses = majorService.getAllMajorList();
        return new ApiResponse<>("전체 학과 반환 완료", majorListResponses);
    }
}
