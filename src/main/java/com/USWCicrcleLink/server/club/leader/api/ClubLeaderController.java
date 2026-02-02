package com.USWCicrcleLink.server.club.leader.api;

import com.USWCicrcleLink.server.category.service.CategoryService;
import com.USWCicrcleLink.server.club.application.dto.ApplicantResultsRequest;
import com.USWCicrcleLink.server.club.dto.ClubCategoryDto;
import com.USWCicrcleLink.server.club.leader.dto.FcmTokenRequest;
import com.USWCicrcleLink.server.club.leader.dto.club.*;
import com.USWCicrcleLink.server.club.leader.dto.clubMembers.*;
import com.USWCicrcleLink.server.club.application.dto.AplictDto;
import com.USWCicrcleLink.server.club.leader.service.ClubLeaderService;
import com.USWCicrcleLink.server.club.leader.service.FcmServiceImpl;
import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.ProfileException;
import com.USWCicrcleLink.server.global.response.ApiResponse;
import com.USWCicrcleLink.server.global.validation.ValidationSequence;
import com.USWCicrcleLink.server.user.profile.domain.MemberType;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/club-leader")
@Slf4j
@Tag(name = "Club Leader", description = "동아리 회장 기능 API")
public class ClubLeaderController {

    private final ClubLeaderService clubLeaderService;
    private final CategoryService categoryService;
    private final FcmServiceImpl fcmService;

    // 약관 동의 완료 업데이트
    @PatchMapping("/terms/agreement")
    public ResponseEntity<ApiResponse<String>> SetAgreedTermsTrue() {
        clubLeaderService.updateAgreedTermsTrue();
        return new ResponseEntity<>(new ApiResponse<>("약관 동의 완료"), HttpStatus.OK);
    }

    // 동아리 기본 정보 조회
    @GetMapping("/{clubUUID}/info")
    public ResponseEntity<ApiResponse> getClubInfo(@PathVariable("clubUUID") UUID clubUUID) {
        ApiResponse<ClubInfoResponse> clubInfo = clubLeaderService.getClubInfo(clubUUID);
        return new ResponseEntity<>(clubInfo, HttpStatus.OK);
    }

    // 동아리 기본 정보 변경 - 카테고리 조회
    @GetMapping("/category")
    public ResponseEntity<ApiResponse<List<ClubCategoryDto>>> getAllCategories() {
        List<ClubCategoryDto> categories = categoryService.getAllClubCategories();
        return ResponseEntity.ok(new ApiResponse<>("카테고리 리스트 조회 성공", categories));
    }

    // 동아리 기본 정보 변경
    @PutMapping("/{clubUUID}/info")
    public ResponseEntity<ApiResponse> updateClubInfo(@PathVariable("clubUUID") UUID clubUUID,
            @RequestPart(value = "mainPhoto", required = false) MultipartFile mainPhoto,
            @RequestPart(value = "clubInfoRequest", required = false) @Validated(ValidationSequence.class) ClubInfoRequest clubInfoRequest,
            @RequestPart(value = "leaderUpdatePwRequest", required = false) @Validated(ValidationSequence.class) com.USWCicrcleLink.server.club.leader.dto.LeaderUpdatePwRequest leaderUpdatePwRequest,
            HttpServletResponse response) throws IOException {

        ApiResponse result = clubLeaderService.updateClubInfo(clubUUID, clubInfoRequest, mainPhoto);
        if (leaderUpdatePwRequest != null) {
            clubLeaderService.updatePassword(leaderUpdatePwRequest, response);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    // 동아리 요약 조회
    @GetMapping("/{clubUUID}/summary")
    public ResponseEntity<ApiResponse<ClubSummaryResponse>> getClubSummary(@PathVariable("clubUUID") UUID clubUUID) {
        ClubSummaryResponse clubIntroWebResponse = clubLeaderService.getClubSummary(clubUUID);
        ApiResponse<ClubSummaryResponse> response = new ApiResponse<>("동아리 요약 조회 완료", clubIntroWebResponse);
        return ResponseEntity.ok(response);
    }

    // 동아리 소개 조회
    @GetMapping("/{clubUUID}/intro")
    public ResponseEntity<ApiResponse<LeaderClubIntroResponse>> getClubIntro(@PathVariable("clubUUID") UUID clubUUID) {
        return new ResponseEntity<>(clubLeaderService.getClubIntro(clubUUID), HttpStatus.OK);
    }

    // 동아리 소개 변경
    @PutMapping("/{clubUUID}/intro")
    public ResponseEntity<ApiResponse> updateClubIntro(@PathVariable("clubUUID") UUID clubUUID,
            @RequestPart(value = "clubIntroRequest", required = false) @Valid ClubIntroRequest clubIntroRequest,
            @RequestPart(value = "introPhotos", required = false) List<MultipartFile> introPhotos) throws IOException {

        return new ResponseEntity<>(clubLeaderService.updateClubIntro(clubUUID, clubIntroRequest, introPhotos),
                HttpStatus.OK);
    }

    // 동아리 모집 상태 변경
    @PatchMapping("/{clubUUID}/recruitment")
    public ResponseEntity<ApiResponse> toggleRecruitmentStatus(@PathVariable("clubUUID") UUID clubUUID) {
        return new ResponseEntity<>(clubLeaderService.toggleRecruitmentStatus(clubUUID), HttpStatus.OK);
    }

    // 소속 동아리 회원 조회
    @GetMapping("/{clubUUID}/members")
    public ResponseEntity<ApiResponse> getClubMembers(
            @PathVariable("clubUUID") UUID clubUUID,
            @RequestParam(value = "sort", defaultValue = "default") String sort) {

        ApiResponse<List<ClubMembersResponse>> response = switch (sort.toLowerCase()) {
            case "regular-member" -> clubLeaderService.getClubMembersByMemberType(clubUUID, MemberType.REGULARMEMBER);
            case "default" -> clubLeaderService.getClubMembers(clubUUID);
            default -> throw new ProfileException(ExceptionType.INVALID_MEMBER_TYPE);
        };

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 동아리 회원 퇴출
    @DeleteMapping("/{clubUUID}/members")
    public ResponseEntity<ApiResponse> deleteClubMembers(@PathVariable("clubUUID") UUID clubUUID,
            @RequestBody List<ClubMembersDeleteRequest> clubMemberUUIDList) {
        return new ResponseEntity<>(clubLeaderService.deleteClubMembers(clubUUID, clubMemberUUIDList), HttpStatus.OK);
    }

    // fcm 토큰 갱신
    @PatchMapping("/fcmtoken")
    public ResponseEntity<ApiResponse> updateFcmToken(@RequestBody FcmTokenRequest fcmTokenRequest) {
        fcmService.refreshFcmToken(fcmTokenRequest);
        return new ResponseEntity<>(new ApiResponse<>("fcm token 갱신 완료"), HttpStatus.OK);
    }

    // 지원자 조회
    @GetMapping("/{clubUUID}/applicants")
    public ResponseEntity<ApiResponse> getApplicants(
            @PathVariable("clubUUID") UUID clubUUID,
            @RequestParam(value = "status", required = false) com.USWCicrcleLink.server.club.application.domain.AplictStatus status) {
        return new ResponseEntity<>(clubLeaderService.getApplicants(clubUUID, status), HttpStatus.OK);
    }

    // 합격자 알림
    @PostMapping("/{clubUUID}/applicants/notifications")
    public ResponseEntity<ApiResponse> pushApplicantResults(@PathVariable("clubUUID") UUID clubUUID,
            @RequestBody @Validated(ValidationSequence.class) List<ApplicantResultsRequest> results)
            throws IOException {
        clubLeaderService.updateApplicantResults(clubUUID, results);
        return new ResponseEntity<>(new ApiResponse<>("지원 결과 처리 완료"), HttpStatus.OK);
    }

    // 지원서 상세 조회
    @GetMapping("/{clubUUID}/applications/{applicationUUID}")
    public ResponseEntity<ApiResponse<AplictDto.DetailResponse>> getApplicationDetail(
            @PathVariable("clubUUID") UUID clubUUID,
            @PathVariable("applicationUUID") UUID applicationUUID) {
        return ResponseEntity.ok(new ApiResponse<>("지원서 상세 조회 완료",
                clubLeaderService.getApplicationDetail(clubUUID, applicationUUID)));
    }

    // 지원자 상태 변경
    @PatchMapping("/{clubUUID}/applications/{applicationUUID}/status")
    public ResponseEntity<ApiResponse<Void>> updateApplicationStatus(
            @PathVariable("clubUUID") UUID clubUUID,
            @PathVariable("applicationUUID") UUID applicationUUID,
            @RequestBody @Valid AplictDto.UpdateStatusRequest request) {
        clubLeaderService.updateAplictStatus(clubUUID, applicationUUID, request.getStatus());
        return ResponseEntity.ok(new ApiResponse<>("지원자 상태 변경 완료"));
    }
}
