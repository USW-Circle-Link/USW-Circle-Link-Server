package com.USWCicrcleLink.server.aplict.controller;

import com.USWCicrcleLink.server.aplict.domain.Aplict;
import com.USWCicrcleLink.server.aplict.dto.AplictRequest;
import com.USWCicrcleLink.server.aplict.service.AplictService;
import com.USWCicrcleLink.server.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/aplict")
public class AplictController {
    private final AplictService aplictService;

    // 지원서 제출하기
    @PostMapping("/submit")
    public ResponseEntity<ApiResponse> submitAplict(@RequestBody AplictRequest request) {
        aplictService.submitAplict(request);
        ApiResponse response = new ApiResponse("지원서 제출 성공");
        return ResponseEntity.ok(response);
    }

    //해당동아리지원서조회
    @GetMapping("/club/{clubId}")
    public ResponseEntity<ApiResponse> getAplictByClubId(@PathVariable("clubId") Long clubId) {
        List<Aplict> aplicts = aplictService.getAplictByClubId(clubId);
        ApiResponse response = new ApiResponse("지원서 조회 성공", aplicts);
        return ResponseEntity.ok(response);
    }
}