package com.USWCicrcleLink.server.user.api;

import com.USWCicrcleLink.server.global.email.service.EmailTokenService;
import com.USWCicrcleLink.server.global.bucket4j.RateLimite;
import com.USWCicrcleLink.server.global.exception.errortype.EmailTokenException;
import com.USWCicrcleLink.server.global.response.ApiResponse;

import com.USWCicrcleLink.server.global.validation.ValidationSequence;

import com.USWCicrcleLink.server.user.dto.*;
import com.USWCicrcleLink.server.user.service.AuthTokenService;
import com.USWCicrcleLink.server.user.service.UserService;
import com.USWCicrcleLink.server.user.service.WithdrawalTokenService;
import com.USWCicrcleLink.server.user.service.MypageService;
import com.USWCicrcleLink.server.user.profile.service.ProfileService;
import com.USWCicrcleLink.server.user.profile.dto.ProfileResponse;
import com.USWCicrcleLink.server.user.profile.dto.ProfileRequest;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Slf4j
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "일반 사용자 관련 API")
public class UserController {

    private final UserService userService;
    private final AuthTokenService authTokenService;
    private final WithdrawalTokenService withdrawalTokenService;
    private final EmailTokenService emailTokenService;
    private final ProfileService profileService;
    private final MypageService mypageService;

    // 내 프로필 조회
    @GetMapping("/me")
    public ApiResponse<ProfileResponse> getMyProfile() {
        ProfileResponse profileResponse = profileService.getMyProfile();
        return new ApiResponse<>("프로필 조회 성공", profileResponse);
    }

    // 내 프로필 수정
    @PatchMapping("/me")
    public ApiResponse<ProfileResponse> updateProfile(
            @RequestBody @Validated(ValidationSequence.class) ProfileRequest profileRequest) {
        ProfileResponse profileResponse = profileService.updateProfile(profileRequest);
        return new ApiResponse<>("프로필 수정 성공", profileResponse);
    }

    // 내가 소속된 동아리 목록 조회
    @GetMapping("/me/clubs")
    public ApiResponse<List<MyClubResponse>> getMyClubById() {
        List<MyClubResponse> myclubs = mypageService.getMyClubById();
        return new ApiResponse<>("소속된 동아리 목록 조회 성공", myclubs);
    }

    // 내가 지원한 동아리(지원서) 목록 조회
    @GetMapping("/me/applications")
    public ApiResponse<List<MyAplictResponse>> getAplictClubById() {
        List<MyAplictResponse> aplictClubs = mypageService.getAplictClubById();
        return new ApiResponse<>("지원한 동아리 목록 조회 성공", aplictClubs);
    }

    @PatchMapping("/me/password")
    public ApiResponse<Void> updateUserPw(@Validated(ValidationSequence.class) @RequestBody UpdatePwRequest request) {
        userService.updateNewPW(request);
        return new ApiResponse<>("비밀번호가 성공적으로 업데이트 되었습니다.");
    }

    // 이메일 인증 여부 검증하기
    @GetMapping("/email/verify-token")
    public ModelAndView verifySignUpMail(@RequestParam("emailTokenUUID") UUID emailTokenUUID) {

        ModelAndView modelAndView = new ModelAndView();

        try {
            // 제한시간 안에 인증에 성공
            userService.verifyEmailToken(emailTokenUUID);
            modelAndView.setViewName("success");
        } catch (EmailTokenException e) {
            // 이메일 만료 시간이 지난경우
            modelAndView.setViewName("expired");
        } catch (Exception e) {
            // 예상치 못한 다른 예외
            modelAndView.setViewName("failure");
        }
        return modelAndView;
    }

    // 회원 탈퇴 인증 번호 확인
    @DeleteMapping("/me")
    @RateLimite(action = "WITHDRAWAL_CODE")
    public ApiResponse<Void> cancelMembership(HttpServletRequest request, HttpServletResponse response,
            @Valid @RequestBody AuthCodeRequest authCodeRequest) {

        // 토큰 검증 및 삭제
        UUID uuid = withdrawalTokenService.verifyWithdrawalToken(authCodeRequest);
        withdrawalTokenService.deleteWithdrawalToken(uuid);

        // 인증 토큰 존재시 인증 토큰도 삭제
        authTokenService.delete(uuid);

        // 회원 탈퇴 진행
        userService.cancelMembership(request, response);

        return new ApiResponse<>("회원 탈퇴가 완료되었습니다.");
    }

    // 프로필 중복 확인 (From ProfileController)
    @PostMapping("/profile/duplication-check")
    public ApiResponse<com.USWCicrcleLink.server.user.profile.dto.ProfileDuplicationCheckResponse> checkDuplication(
            @RequestBody @Validated(ValidationSequence.class) com.USWCicrcleLink.server.user.profile.dto.ProfileDuplicationCheckRequest request) {
        com.USWCicrcleLink.server.user.profile.dto.ProfileDuplicationCheckResponse response = profileService
                .checkProfileDuplication(
                        request.getUserName(),
                        request.getStudentNumber(),
                        request.getUserHp(),
                        request.getClubUUID());
        return new ApiResponse<>("프로필 중복 확인 결과", response);
    }

    // 동아리방 층별 사진 조회 (From MypageController)
    @GetMapping("/clubs/{floor}/photo")
    public ApiResponse<com.USWCicrcleLink.server.user.dto.ClubFloorPhotoResponse> getClubFloorPhoto(
            @PathVariable("floor") String floor) {
        com.USWCicrcleLink.server.user.dto.ClubFloorPhotoResponse clubFloorPhotoResponse = mypageService
                .getClubFloorPhoto(floor);
        return new ApiResponse<>("동아리방 층별 사진 조회 성공", clubFloorPhotoResponse);
    }
}
