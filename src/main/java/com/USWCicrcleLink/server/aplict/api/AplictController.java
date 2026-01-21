package com.USWCicrcleLink.server.aplict.api;

import com.USWCicrcleLink.server.aplict.dto.AplictDetailResponse;
import com.USWCicrcleLink.server.aplict.dto.SubmitAplictRequest;
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

    // 지원 가능 여부 확인 (ANYONE)
    @GetMapping("/can-apply/{clubUUID}")
    public ResponseEntity<ApiResponse<Boolean>> canApply(@PathVariable("clubUUID") UUID clubUUID) {
        boolean canApply = aplictService.canApply(clubUUID);
        return ResponseEntity.ok(new ApiResponse<>("지원 가능", canApply));
    }

    // (Legacy) 구글 폼 URL 조회 (USER)
    @GetMapping("/{clubUUID}")
    public ResponseEntity<ApiResponse<String>> getGoogleFormUrl(@PathVariable("clubUUID") UUID clubUUID) {
        String googleFormUrl = aplictService.getGoogleFormUrlByClubUUID(clubUUID);
        return ResponseEntity.ok(new ApiResponse<>("구글 폼 URL 조회 성공", googleFormUrl));
    }

    //  동아리 지원서 제출 (자체 폼 제출)
    @PostMapping("/{clubUUID}/forms/{formId}")
    public ResponseEntity<ApiResponse<Void>> submitAplict(
            @PathVariable("clubUUID") UUID clubUUID,
            @PathVariable("formId") Long formId,
            @RequestBody SubmitAplictRequest request) {

        aplictService.submitAplict(clubUUID, formId, request);
        return ResponseEntity.ok(new ApiResponse<>("지원서 제출 성공"));
    }


}