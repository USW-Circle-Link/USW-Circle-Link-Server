package com.USWCicrcleLink.server.aplict.api;

import com.USWCicrcleLink.server.aplict.dto.AplictRequest;
import com.USWCicrcleLink.server.aplict.service.AplictService;
import com.USWCicrcleLink.server.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/apply")
public class AplictController {
    private final AplictService aplictService;

    //지원서 작성하기(구글 폼 반환)
    @GetMapping("/{clubId}")
    public ResponseEntity<ApiResponse<String>> getGoogleFormUrl(@PathVariable("clubId") Long clubId) {
        String googleFormUrl = aplictService.getGoogleFormUrlByClubId(clubId);
        ApiResponse<String> response = new ApiResponse<>("구글 폼 URL 조회 성공", googleFormUrl);
        return ResponseEntity.ok(response);
    }

    //동아리 지원서 제출
    @PostMapping("/{clubId}")
    public ResponseEntity<ApiResponse<Void>> submitAplict(
            @RequestHeader("User-uuid") UUID userUUID,
            @PathVariable("clubId") Long clubId,
            @RequestBody AplictRequest request) {
        aplictService.submitAplict(userUUID, clubId, request);
        ApiResponse<Void> response = new ApiResponse<>("지원서 제출 성공");
        return ResponseEntity.ok(response);
    }
}