package com.USWCicrcleLink.server.clubLeader.api;

import com.USWCicrcleLink.server.clubLeader.dto.FormDto;
import com.USWCicrcleLink.server.clubLeader.service.FormService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/clubs")
public class FormController {

    private final FormService formService;

    // 1️ 지원서 폼 생성
    @PostMapping("/{clubId}/forms")
    public ResponseEntity<Void> createForm(
            @PathVariable Long clubId,
            @RequestBody @Valid FormDto.CreateRequest request
    ) {
        Long formId = formService.createForm(clubId, request);
        return ResponseEntity.created(URI.create("/api/clubs/" + clubId + "/forms/" + formId)).build();
    }

    // 2 지원서 상태 변경
    @PatchMapping("/{clubId}/forms/{formId}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable Long clubId,
            @PathVariable Long formId,
            @RequestBody @Valid FormDto.UpdateStatusRequest request
    ) {
        formService.updateStatus(clubId, formId, request);
        return ResponseEntity.ok().build();
    }
}