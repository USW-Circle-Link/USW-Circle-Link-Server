package com.USWCicrcleLink.server.club.form.api;

import com.USWCicrcleLink.server.club.form.dto.ClubFormResponse;
import com.USWCicrcleLink.server.club.form.service.ClubFormService;
import com.USWCicrcleLink.server.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/clubs/forms")
public class ClubFormController {

    private final ClubFormService clubFormService;

    /**
     * 특정 동아리의 현재 활성화된(PUBLISHED) 지원서 양식 조회
     * 경로: GET /clubs/forms/{clubUUID}
     * 예시: GET /clubs/forms/be628283-11d4-48f4-a2ab-2c75b5c79b78
     */
    @GetMapping("/{clubUUID}")
    public ResponseEntity<ApiResponse<ClubFormResponse>> getActiveForm(@PathVariable UUID clubUUID) {
        ClubFormResponse response = clubFormService.getQuestionsByClub(clubUUID);
        return ResponseEntity.ok(new ApiResponse<>("활성화된 지원서 조회 완료", response));
    }
}
