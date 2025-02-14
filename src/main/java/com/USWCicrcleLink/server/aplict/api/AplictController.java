package com.USWCicrcleLink.server.aplict.api;

import com.USWCicrcleLink.server.aplict.dto.AplictRequest;
import com.USWCicrcleLink.server.aplict.service.AplictService;
import com.USWCicrcleLink.server.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/apply")
public class AplictController {
    private final AplictService aplictService;

    // 지원 가능 여부 확인 (모바일)
    @GetMapping("/can-apply/{clubUUID}")
    public ResponseEntity<ApiResponse<Boolean>> canApply(@PathVariable("clubUUID") UUID clubUUID) {
        aplictService.checkIfCanApply(clubUUID);
        return ResponseEntity.ok(new ApiResponse<>("지원 가능"));
    }

    //구글 폼 URL 조회 (모바일)
    @GetMapping("/{clubUUID}")
    public ResponseEntity<ApiResponse<String>> getGoogleFormUrl(@PathVariable("clubUUID") UUID clubUUID) {
        String googleFormUrl = aplictService.getGoogleFormUrlByClubUUID(clubUUID);
        return ResponseEntity.ok(new ApiResponse<>("구글 폼 URL 조회 성공", googleFormUrl));
    }

    //동아리 지원서 제출 (모바일)
    @PostMapping("/{clubUUID}")
    public ResponseEntity<ApiResponse<Void>> submitAplict(
            @PathVariable("clubUUID") UUID clubUUID,
            @RequestBody @Valid AplictRequest request) {
        aplictService.submitAplict(clubUUID, request);
        return ResponseEntity.ok(new ApiResponse<>("지원서 제출 성공"));
    }
}