package com.USWCicrcleLink.server.club.application.api;

import com.USWCicrcleLink.server.club.application.service.AplictService;
import com.USWCicrcleLink.server.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.USWCicrcleLink.server.club.application.dto.AplictDto;
import jakarta.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/clubs/{clubUUID}/applications")
@Tag(name = "Club Application", description = "동아리 지원 관련 API")
public class AplictController {
    private final AplictService aplictService;

    // 지원 가능 여부 조회
    @GetMapping("/eligibility")
    public ResponseEntity<ApiResponse<Boolean>> checkEligibility(@PathVariable("clubUUID") UUID clubUUID) {
        boolean canApply = aplictService.canApply(clubUUID);
        return ResponseEntity.ok(new ApiResponse<>("지원 가능", canApply));
    }

    // 지원서 제출
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> submitApplication(@PathVariable("clubUUID") UUID clubUUID,
            @RequestBody @Valid AplictDto.SubmitRequest request) {
        aplictService.submitAplict(clubUUID, request);
        return ResponseEntity.ok(new ApiResponse<>("지원서 제출 성공"));
    }

    // 지원서 상세 조회 (본인)
    @GetMapping("/{aplictUUID}")
    public ResponseEntity<ApiResponse<AplictDto.DetailResponse>> getApplicationDetail(
            @PathVariable("clubUUID") UUID clubUUID,
            @PathVariable("aplictUUID") UUID aplictUUID) {
        AplictDto.DetailResponse response = aplictService.getApplicationDetail(aplictUUID);
        return ResponseEntity.ok(new ApiResponse<>("지원서 상세 조회 성공", response));
    }
}
