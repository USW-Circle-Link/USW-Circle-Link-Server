package com.USWCicrcleLink.server.aplict.api;

import com.USWCicrcleLink.server.aplict.dto.UserApplicationResponse;
import com.USWCicrcleLink.server.aplict.dto.SubmitAplictRequest;
import com.USWCicrcleLink.server.aplict.service.AplictService;
import com.USWCicrcleLink.server.global.response.ApiResponse;
import com.USWCicrcleLink.server.global.security.details.CustomUserDetails;
import com.USWCicrcleLink.server.profile.domain.Profile;
import com.USWCicrcleLink.server.profile.repository.ProfileRepository;
import com.USWCicrcleLink.server.global.exception.errortype.UserException;
import com.USWCicrcleLink.server.global.exception.ExceptionType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/apply")
public class AplictController {

    private final AplictService aplictService;
    private final ProfileRepository profileRepository;

    // 지원 가능 여부 확인 (ANYONE)
    @GetMapping("/can-apply/{clubUUID}")
    public ResponseEntity<ApiResponse<Boolean>> canApply(@PathVariable("clubUUID") UUID clubUUID) {
        boolean canApply = aplictService.canApply(clubUUID);
        return ResponseEntity.ok(new ApiResponse<>("지원 가능 여부 확인 성공", canApply));
    }

    // 구글 폼 URL 조회 (USER)
    @GetMapping("/{clubUUID}")
    public ResponseEntity<ApiResponse<String>> getGoogleFormUrl(@PathVariable("clubUUID") UUID clubUUID) {
        String googleFormUrl = aplictService.getGoogleFormUrlByClubUUID(clubUUID);
        return ResponseEntity.ok(new ApiResponse<>("구글 폼 URL 조회 성공", googleFormUrl));
    }

    // 동아리 지원서 제출 (자체 폼 제출)
    @PostMapping("/{clubUUID}/forms/{formId}")
    public ResponseEntity<ApiResponse<Void>> submitAplict(
            @PathVariable("clubUUID") UUID clubUUID,
            @PathVariable("formId") Long formId,
            @RequestBody SubmitAplictRequest request) {

        aplictService.submitAplict(clubUUID, formId, request);
        return ResponseEntity.ok(new ApiResponse<>("지원서 제출 성공"));
    }

    // 내 지원서 상세 조회 (USER)
    @GetMapping("/applications/{aplictId}")
    public ResponseEntity<ApiResponse<List<UserApplicationResponse>>> getMyApplication(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long aplictId) {

        // 1. 유저 정보로 프로필 조회
        Profile profile = profileRepository.findByUser_UserUUID(userDetails.user().getUserUUID())
                .orElseThrow(() -> new UserException(ExceptionType.USER_NOT_EXISTS));

        // 2. 서비스 로직 호출 (지원 답변 목록 가져오기)
        List<UserApplicationResponse> response = aplictService.getMyApplicationAnswers(profile.getProfileId(), aplictId);

        return ResponseEntity.ok(new ApiResponse<>("지원서 상세 조회 성공", response));
    }
}