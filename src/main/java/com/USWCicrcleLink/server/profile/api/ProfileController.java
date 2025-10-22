package com.USWCicrcleLink.server.profile.api;

import com.USWCicrcleLink.server.global.response.ApiResponse;
import com.USWCicrcleLink.server.global.validation.ValidationSequence;
import com.USWCicrcleLink.server.profile.dto.ProfileDuplicationCheckRequest;
import com.USWCicrcleLink.server.profile.dto.ProfileDuplicationCheckResponse;
import com.USWCicrcleLink.server.profile.dto.ProfileRequest;
import com.USWCicrcleLink.server.profile.dto.ProfileResponse;
import com.USWCicrcleLink.server.profile.service.ProfileService;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
    @Operation(
            summary = "프로필 중복 확인",
            description = "이름(userName), 학번(studentNumber), 전화번호(userHp)로 기존 프로필의 존재 여부를 확인합니다. 선택적으로 clubUUID를 전달하면 대상 동아리 소속 여부 및 분류(SAME_CLUB/OTHER_CLUB/NO_CLUB)를 함께 반환합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "프로필 중복 확인 요청 본문",
                    content = @Content(schema = @Schema(implementation = ProfileDuplicationCheckRequest.class))
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "중복 확인 결과",
                    content = @Content(schema = @Schema(implementation = ProfileDuplicationCheckResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효성 검증 실패")
    })
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
