package com.USWCicrcleLink.server.club.api;

import com.USWCicrcleLink.server.admin.dto.AdminClubCreationRequest;
import com.USWCicrcleLink.server.admin.dto.AdminClubIntroResponse;

import com.USWCicrcleLink.server.admin.dto.AdminPwRequest;
import com.USWCicrcleLink.server.admin.service.AdminClubService;
import com.USWCicrcleLink.server.club.application.dto.ApplicantResultsRequest;

import com.USWCicrcleLink.server.club.dto.ClubListByClubCategoryResponse;
import com.USWCicrcleLink.server.club.dto.ClubListResponse;
import com.USWCicrcleLink.server.club.service.ClubService;
import com.USWCicrcleLink.server.club.leader.dto.FcmTokenRequest;
import com.USWCicrcleLink.server.club.leader.dto.club.ClubInfoRequest;
import com.USWCicrcleLink.server.club.leader.dto.club.ClubInfoResponse;
import com.USWCicrcleLink.server.club.leader.dto.clubMembers.ClubMembersResponse;
import com.USWCicrcleLink.server.club.leader.dto.clubMembers.ClubMembersDeleteRequest;
import com.USWCicrcleLink.server.club.leader.service.ClubLeaderService;
import com.USWCicrcleLink.server.club.leader.service.FcmServiceImpl;
import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.ProfileException;
import com.USWCicrcleLink.server.global.response.ApiResponse;
import com.USWCicrcleLink.server.global.validation.ValidationSequence;
import com.USWCicrcleLink.server.user.profile.domain.MemberType;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/clubs")
@Slf4j
@Tag(name = "Clubs", description = "동아리 관련 API (통합)")
public class ClubController {

    private final ClubService clubService;
    private final AdminClubService adminClubService;
    private final ClubLeaderService clubLeaderService;
    private final FcmServiceImpl fcmService;

    // --- Public / General Endpoints ---

    // 전체 동아리 조회 (모바일 - 리스트)
    @GetMapping
    public ResponseEntity<ApiResponse<List<ClubListResponse>>> getAllClubs() {
        List<ClubListResponse> clubs = clubService.getAllClubs();
        return ResponseEntity.ok(new ApiResponse<>("전체 동아리 조회 완료", clubs));
    }

    // 전체 동아리 조회 (모바일 - 리스트, 카테고리 필터)
    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<List<ClubListByClubCategoryResponse>>> getAllClubsByClubCategories(
            @RequestParam(name = "clubCategoryUUIDs", defaultValue = "") List<UUID> clubCategoryUUIDs) {
        List<ClubListByClubCategoryResponse> clubs = clubService.getAllClubsByClubCategories(clubCategoryUUIDs);
        return ResponseEntity.ok(new ApiResponse<>("카테고리별 전체 동아리 조회 완료", clubs));
    }

    // 모집 중인 동아리 조회
    @GetMapping("/open")
    public ResponseEntity<ApiResponse<List<ClubListResponse>>> getOpenClubs() {
        List<ClubListResponse> clubs = clubService.getOpenClubs();
        return ResponseEntity.ok(new ApiResponse<>("모집 중인 동아리 조회 완료", clubs));
    }

    // 모집 중인 동아리 조회 (카테고리 필터)
    @GetMapping("/open/filter")
    public ResponseEntity<ApiResponse<List<ClubListByClubCategoryResponse>>> getOpenClubsByCategories(
            @RequestParam(name = "clubCategoryUUIDs", defaultValue = "") List<UUID> clubCategoryUUIDs) {
        List<ClubListByClubCategoryResponse> clubs = clubService.getOpenClubsByClubCategories(clubCategoryUUIDs);
        return ResponseEntity.ok(new ApiResponse<>("카테고리별 모집 중인 동아리 조회 완료", clubs));
    }

    // 동아리 상세 조회 (소개글 - Public)
    @GetMapping("/{clubUUID}")
    public ResponseEntity<ApiResponse<AdminClubIntroResponse>> getClubIntroByClubId(
            @PathVariable("clubUUID") UUID clubUUID) {
        AdminClubIntroResponse clubIntroResponse = clubService.getClubIntro(clubUUID);
        return ResponseEntity.ok(new ApiResponse<>("동아리 소개글 조회 성공", clubIntroResponse));
    }

    // --- Admin Endpoints ---

    // 동아리 생성
    @PostMapping
    public ResponseEntity<ApiResponse<String>> createClub(
            @RequestBody @Validated(ValidationSequence.class) AdminClubCreationRequest clubRequest) {
        adminClubService.createClub(clubRequest);
        return ResponseEntity.ok(new ApiResponse<>("동아리 생성 성공"));
    }

    // 동아리 삭제
    @DeleteMapping("/{clubUUID}")
    public ResponseEntity<ApiResponse<Long>> deleteClub(@PathVariable("clubUUID") UUID clubUUID,
            @RequestBody(required = false) @Validated(ValidationSequence.class) AdminPwRequest request) {
        // AdminPwRequest checks might be specific to implementation.
        // If request is null, it might be an issue if service requires it.
        // Assuming Admin context validation.
        adminClubService.deleteClub(clubUUID, request);
        return ResponseEntity.ok(new ApiResponse<>("동아리 삭제 성공"));
    }

    // 중복 확인 (회장 ID or 동아리 이름)
    @GetMapping("/check-duplication")
    public ResponseEntity<ApiResponse<String>> checkDuplication(
            @RequestParam("type") String type,
            @RequestParam("val") String val) {
        if ("LEADER".equalsIgnoreCase(type)) {
            adminClubService.validateLeaderAccount(val);
            return ResponseEntity.ok(new ApiResponse<>("사용 가능한 동아리 회장 아이디입니다."));
        } else if ("NAME".equalsIgnoreCase(type)) {
            adminClubService.validateClubName(val);
            return ResponseEntity.ok(new ApiResponse<>("사용 가능한 동아리 이름입니다."));
        } else {
            return ResponseEntity.badRequest().body(new ApiResponse<>("잘못된 중복 확인 타입입니다. (LEADER/NAME)"));
        }
    }

    // --- Club Leader Endpoints ---

    // 약관 동의 (Leader)
    @PatchMapping("/terms/agreement")
    public ResponseEntity<ApiResponse<String>> setAgreedTermsTrue() {
        clubLeaderService.updateAgreedTermsTrue();
        return ResponseEntity.ok(new ApiResponse<>("약관 동의 완료"));
    }

    // 동아리 관리 정보 조회 (Leader)
    @GetMapping("/{clubUUID}/info")
    public ResponseEntity<ApiResponse<ClubInfoResponse>> getClubInfo(@PathVariable("clubUUID") UUID clubUUID) {
        ApiResponse<ClubInfoResponse> clubInfo = clubLeaderService.getClubInfo(clubUUID);
        return ResponseEntity.ok(clubInfo);
    }

    // 동아리 관리 정보 수정 (Leader)
    @PutMapping("/{clubUUID}")
    public ResponseEntity<ApiResponse> updateClubInfo(
            @PathVariable("clubUUID") UUID clubUUID,
            @RequestPart(value = "mainPhoto", required = false) MultipartFile mainPhoto,
            @RequestPart(value = "clubInfoRequest", required = false) @Validated(ValidationSequence.class) ClubInfoRequest clubInfoRequest,
            @RequestPart(value = "leaderUpdatePwRequest", required = false) @Validated(ValidationSequence.class) com.USWCicrcleLink.server.club.leader.dto.LeaderUpdatePwRequest leaderUpdatePwRequest,
            HttpServletResponse response) throws IOException {

        ApiResponse result = clubLeaderService.updateClubInfo(clubUUID, clubInfoRequest, mainPhoto);
        if (leaderUpdatePwRequest != null) {
            clubLeaderService.updatePassword(leaderUpdatePwRequest, response);
        }
        return ResponseEntity.ok(result);
    }

    // 모집 상태 조회
    // 모집 상태 조회
    @GetMapping("/{clubUUID}/recruit-status")
    public ResponseEntity<ApiResponse> getRecruitmentStatus(@PathVariable("clubUUID") UUID clubUUID) {
        return ResponseEntity.ok(clubLeaderService.getRecruitmentStatus(clubUUID));
    }

    // 모집 상태 변경 (Toggle)
    @PatchMapping("/{clubUUID}/recruit-status")
    public ResponseEntity<ApiResponse> toggleRecruitmentStatus(@PathVariable("clubUUID") UUID clubUUID) {
        return new ResponseEntity<>(clubLeaderService.toggleRecruitmentStatus(clubUUID), HttpStatus.OK);
    }

    // 동아리 회원 조회 (Leader)
    @GetMapping("/{clubUUID}/members")
    public ResponseEntity<ApiResponse> getClubMembers(
            @PathVariable("clubUUID") UUID clubUUID,
            @RequestParam(value = "sort", defaultValue = "default") String sort) {

        ApiResponse<List<ClubMembersResponse>> response = switch (sort.toLowerCase()) {
            case "regular-member" -> clubLeaderService.getClubMembersByMemberType(clubUUID, MemberType.REGULARMEMBER);
            case "default" -> clubLeaderService.getClubMembers(clubUUID);
            default -> throw new ProfileException(ExceptionType.INVALID_MEMBER_TYPE);
        };
        return ResponseEntity.ok(response);
    }

    // 동아리 회원 삭제 (Leader)
    @DeleteMapping("/{clubUUID}/members")
    public ResponseEntity<ApiResponse> deleteClubMembers(@PathVariable("clubUUID") UUID clubUUID,
            @RequestBody List<ClubMembersDeleteRequest> clubMemberUUIDList) {
        return new ResponseEntity<>(clubLeaderService.deleteClubMembers(clubUUID, clubMemberUUIDList), HttpStatus.OK);
    }

    // 최초 지원자 조회
    @GetMapping("/{clubUUID}/applicants")
    public ResponseEntity<ApiResponse> getApplicants(
            @PathVariable("clubUUID") UUID clubUUID,
            @RequestParam(value = "status", required = false) com.USWCicrcleLink.server.club.application.domain.AplictStatus status) {
        return new ResponseEntity<>(clubLeaderService.getApplicants(clubUUID, status), HttpStatus.OK);
    }

    // 최초 합격자 알림
    @PostMapping("/{clubUUID}/applicants/notifications")
    public ResponseEntity<ApiResponse> pushApplicantResults(@PathVariable("clubUUID") UUID clubUUID,
            @RequestBody @Validated(ValidationSequence.class) List<ApplicantResultsRequest> results)
            throws IOException {
        clubLeaderService.updateApplicantResults(clubUUID, results);
        return new ResponseEntity<>(new ApiResponse<>("지원 결과 처리 완료"), HttpStatus.OK);
    }

    // FCM 토큰 갱신 (Leader)
    @PatchMapping("/fcmtoken")
    public ResponseEntity<ApiResponse> updateFcmToken(@RequestBody FcmTokenRequest fcmTokenRequest) {
        fcmService.refreshFcmToken(fcmTokenRequest);
        return new ResponseEntity<>(new ApiResponse<>("fcm token 갱신 완료"), HttpStatus.OK);
    }
}
