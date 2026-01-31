package com.USWCicrcleLink.server.club.leader.api;

import com.USWCicrcleLink.server.club.leader.dto.FormDto;
import com.USWCicrcleLink.server.club.leader.service.FormService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/clubs")
public class FormController {

    private final FormService formService;

    // 1️ 지원서 폼 생성
    @PostMapping("/{clubUUID}/forms")
    public ResponseEntity<Void> createForm(
            @PathVariable UUID clubUUID,
            @RequestBody @Valid FormDto.CreateRequest request) {
        Long formId = formService.createForm(clubUUID, request);
        return ResponseEntity.created(URI.create("/api/clubs/" + clubUUID + "/forms/" + formId)).build();
    }

}
