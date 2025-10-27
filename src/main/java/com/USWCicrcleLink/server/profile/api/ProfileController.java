package com.USWCicrcleLink.server.profile.api;

import com.USWCicrcleLink.server.global.response.ApiResponse;
import com.USWCicrcleLink.server.global.validation.ValidationSequence;
import com.USWCicrcleLink.server.profile.dto.ProfileDuplicationCheckRequest;
import com.USWCicrcleLink.server.profile.dto.ProfileDuplicationCheckResponse;
import com.USWCicrcleLink.server.profile.dto.ProfileRequest;
import com.USWCicrcleLink.server.profile.dto.ProfileResponse;
import com.USWCicrcleLink.server.profile.service.ProfileService;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("profiles")
@RequiredArgsConstructor
@Tag(name = "Profiles", description = "프로필 관리 API")
public class ProfileController {

    private final ProfileService profileService;

    @PatchMapping("/change")
    public ApiResponse<ProfileResponse> updateProfile(@RequestBody @Validated(ValidationSequence.class) ProfileRequest profileRequest) {
        ProfileResponse profileResponse = profileService.updateProfile(profileRequest);
        return new ApiResponse<>("프로필 수정 성공", profileResponse);
    }

    @GetMapping("/me")
    public ApiResponse<ProfileResponse> getMyProfile(){
        ProfileResponse profileResponse = profileService.getMyProfile();
        return new ApiResponse<>("프로필 조회 성공", profileResponse);
    }

    @PostMapping("/duplication-check")
    public ApiResponse<ProfileDuplicationCheckResponse> checkDuplication(@RequestBody @Validated(ValidationSequence.class) ProfileDuplicationCheckRequest request) {
        ProfileDuplicationCheckResponse response = profileService.checkProfileDuplication(
                request.getUserName(),
                request.getStudentNumber(),
                request.getUserHp(),
                request.getClubUUID()
        );
        return new ApiResponse<>("프로필 중복 확인 결과", response);
    }
}
