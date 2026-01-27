package com.USWCicrcleLink.server.club.form.api;

import com.USWCicrcleLink.server.aplict.dto.AplictSubmitRequest;
import com.USWCicrcleLink.server.aplict.service.AplictService;
import com.USWCicrcleLink.server.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/clubs/{clubId}/forms/{formId}/applications")
public class ClubApplicationController {

    private final AplictService aplictService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> submitApplication(
            @PathVariable UUID clubId,
            @PathVariable Long formId,
            @RequestBody AplictSubmitRequest request
    ) {
        aplictService.submitAplictWithAnswers(clubId, formId, request);
        return ResponseEntity.ok(new ApiResponse<>("지원서 제출 성공"));
    }
}
