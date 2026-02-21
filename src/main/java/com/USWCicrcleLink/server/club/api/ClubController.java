package com.USWCicrcleLink.server.club.api;

import com.USWCicrcleLink.server.admin.dto.AdminClubCreationRequest;
import com.USWCicrcleLink.server.admin.dto.AdminClubInfoResponse;

import com.USWCicrcleLink.server.admin.dto.AdminPwRequest;
import com.USWCicrcleLink.server.admin.service.AdminClubService;
import com.USWCicrcleLink.server.club.application.dto.ApplicantResultsRequest;
import com.USWCicrcleLink.server.club.application.dto.ApplicantsResponse;
import com.USWCicrcleLink.server.club.leader.dto.club.UpdateClubResponse;
import com.USWCicrcleLink.server.club.leader.dto.club.RecruitmentStatusResponse;

import com.USWCicrcleLink.server.club.dto.ClubListResponse;
import com.USWCicrcleLink.server.club.form.dto.ClubFormResponse;
import com.USWCicrcleLink.server.club.service.ClubService;
import com.USWCicrcleLink.server.club.leader.dto.FcmTokenRequest;
import com.USWCicrcleLink.server.club.leader.dto.club.ClubInfoRequest;
import com.USWCicrcleLink.server.club.leader.dto.club.ClubProfileRequest;
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
    private final com.USWCicrcleLink.server.club.leader.service.FormService formService;
    private final com.USWCicrcleLink.server.club.form.service.ClubFormService clubFormService;

    // --- Public / General Endpoints ---

    // 동아리 리스트 조회 (필터링 및 검색 포함)
    @GetMapping
    public ResponseEntity<ApiResponse<List<ClubListResponse>>> getClubs(
            @RequestParam(required = false) Boolean open,
            @RequestParam(required = false) List<String> filter,
            @RequestParam(required = false) Boolean adminInfo) {
        List<ClubListResponse> clubs = clubService.searchClubs(open, filter, adminInfo);
        return ResponseEntity.ok(new ApiResponse<>("동아리 리스트 조회 성공", clubs));
    }

    // 동아리 상세 조회 (소개글 - Public)
    @GetMapping("/{clubUUID}")
    public ResponseEntity<ApiResponse<AdminClubInfoResponse>> getClubInfoByClubId(
            @PathVariable("clubUUID") UUID clubUUID) {
        AdminClubInfoResponse clubInfoResponse = clubService.getClubInfo(clubUUID);
        return ResponseEntity.ok(new ApiResponse<>("동아리 소개글 조회 성공", clubInfoResponse));
    }

    // --- Admin Endpoints ---

    // 동아리 생성
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createClub(
            @RequestBody @Validated(ValidationSequence.class) AdminClubCreationRequest clubRequest) {
        adminClubService.createClub(clubRequest);
        return ResponseEntity.ok(new ApiResponse<>("동아리 생성 성공"));
    }

    // 동아리 삭제
    @DeleteMapping("/{clubUUID}")
    public ResponseEntity<ApiResponse<Void>> deleteClub(@PathVariable("clubUUID") UUID clubUUID,
            @RequestBody(required = false) @Validated(ValidationSequence.class) AdminPwRequest request) {
        adminClubService.deleteClub(clubUUID, request);
        return ResponseEntity.ok(new ApiResponse<>("동아리 삭제 성공"));
    }

    // 중복 확인 (회장 ID or 동아리 이름)
    @GetMapping("/check-duplication")
    public ResponseEntity<ApiResponse<Void>> checkDuplication(
            @RequestParam("type") String type,
            @RequestParam("val") String val) {
        if ("LEADER".equalsIgnoreCase(type) || "ACCOUNT".equalsIgnoreCase(type)) {
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
    public ResponseEntity<ApiResponse<Void>> setAgreedTermsTrue() {
        clubLeaderService.updateAgreedTermsTrue();
        return ResponseEntity.ok(new ApiResponse<>("약관 동의 완료"));
    }

    // // 동아리 관리 정보 조회 (Leader) -> getClubProfile (was getClubInfo)
    // @GetMapping("/{clubUUID}/profile")
    // public ResponseEntity<ApiResponse<ClubProfileResponse>>
    // getClubProfile(@PathVariable("clubUUID") UUID clubUUID) {
    // ApiResponse<ClubProfileResponse> clubProfile =
    // clubLeaderService.getClubProfile(clubUUID);
    // return ResponseEntity.ok(clubProfile);
    // }

    // 동아리 정보 수정 (Leader)
    @PutMapping("/{clubUUID}")
    public ResponseEntity<ApiResponse<UpdateClubResponse>> updateClub(
            @PathVariable("clubUUID") UUID clubUUID,
            @RequestPart(value = "mainPhoto", required = false) MultipartFile mainPhoto,
            @RequestPart(value = "clubProfileRequest", required = false) @Validated(ValidationSequence.class) ClubProfileRequest clubProfileRequest,
            @RequestPart(value = "leaderUpdatePwRequest", required = false) @Validated(ValidationSequence.class) com.USWCicrcleLink.server.club.leader.dto.LeaderUpdatePwRequest leaderUpdatePwRequest,
            @RequestPart(value = "clubInfoRequest", required = false) @jakarta.validation.Valid ClubInfoRequest clubInfoRequest,
            @RequestPart(value = "infoPhotos", required = false) List<MultipartFile> infoPhotos,
            HttpServletResponse response) throws IOException {

        ApiResponse<UpdateClubResponse> result = clubLeaderService.updateClub(clubUUID, clubProfileRequest, mainPhoto,
                clubInfoRequest,
                infoPhotos);
        if (leaderUpdatePwRequest != null) {
            clubLeaderService.updatePassword(leaderUpdatePwRequest, response);
        }
        return ResponseEntity.ok(result);
    }

    // 모집 상태 조회
    @GetMapping("/{clubUUID}/recruit-status")
    public ResponseEntity<ApiResponse<RecruitmentStatusResponse>> getRecruitmentStatus(
            @PathVariable("clubUUID") UUID clubUUID) {
        RecruitmentStatusResponse status = clubLeaderService.getRecruitmentStatus(clubUUID);
        return ResponseEntity.ok(new ApiResponse<>("모집 상태 조회 완료", status));
    }

    // 모집 상태 변경 (Toggle)
    @PatchMapping("/{clubUUID}/recruit-status")
    public ResponseEntity<ApiResponse<Void>> toggleRecruitmentStatus(@PathVariable("clubUUID") UUID clubUUID) {
        clubLeaderService.toggleRecruitmentStatus(clubUUID);
        return ResponseEntity.ok(new ApiResponse<>("동아리 모집 상태 변경 완료"));
    }

    // 동아리 회원 조회 (Leader)
    @GetMapping("/{clubUUID}/members")
    public ResponseEntity<ApiResponse<List<ClubMembersResponse>>> getClubMembers(
            @PathVariable("clubUUID") UUID clubUUID,
            @RequestParam(value = "sort", defaultValue = "default") String sort) {

        List<ClubMembersResponse> members = switch (sort.toLowerCase()) {
            case "regular-member" -> clubLeaderService.getClubMembersByMemberType(clubUUID, MemberType.REGULARMEMBER);
            case "default" -> clubLeaderService.getClubMembers(clubUUID);
            default -> throw new ProfileException(ExceptionType.INVALID_MEMBER_TYPE);
        };
        return ResponseEntity.ok(new ApiResponse<>("동아리 회원 조회 완료", members));
    }

    // 동아리 회원 삭제 (Leader)
    @DeleteMapping("/{clubUUID}/members")
    public ResponseEntity<ApiResponse<Void>> deleteClubMembers(@PathVariable("clubUUID") UUID clubUUID,
            @RequestBody List<ClubMembersDeleteRequest> clubMemberUUIDList) {
        clubLeaderService.deleteClubMembers(clubUUID, clubMemberUUIDList);
        return ResponseEntity.ok(new ApiResponse<>("동아리 회원 삭제 완료"));
    }

    // 지원자 조회
    @GetMapping("/{clubUUID}/applicants")
    public ResponseEntity<ApiResponse<List<ApplicantsResponse>>> getApplicants(
            @PathVariable("clubUUID") UUID clubUUID,
            @RequestParam(value = "status", required = false) com.USWCicrcleLink.server.club.application.domain.AplictStatus status,
            @RequestParam(value = "isResultPublished", required = false) Boolean isResultPublished) {
        List<ApplicantsResponse> applicants = clubLeaderService.getApplicants(clubUUID, status, isResultPublished);
        return ResponseEntity.ok(new ApiResponse<>("동아리 지원자 조회 완료", applicants));
    }

    // 합격자 알림
    @PostMapping("/{clubUUID}/applicants/notifications")
    public ResponseEntity<ApiResponse<Void>> pushApplicantResults(@PathVariable("clubUUID") UUID clubUUID,
            @RequestBody @Validated(ValidationSequence.class) List<ApplicantResultsRequest> results)
            throws IOException {
        clubLeaderService.updateApplicantResults(clubUUID, results);
        return ResponseEntity.ok(new ApiResponse<>("지원 결과 처리 완료"));
    }

    // FCM 토큰 갱신 (Leader)
    @PatchMapping("/fcmtoken")
    public ResponseEntity<ApiResponse<Void>> updateFcmToken(@RequestBody FcmTokenRequest fcmTokenRequest) {
        fcmService.refreshFcmToken(fcmTokenRequest);
        return ResponseEntity.ok(new ApiResponse<>("fcm token 갱신 완료"));
    }

    // --- Added from ClubLeaderController ---

    // // 동아리 요약 조회
    // @GetMapping("/{clubUUID}/summary")
    // public
    // ResponseEntity<ApiResponse<com.USWCicrcleLink.server.club.leader.dto.club.ClubSummaryResponse>>
    // getClubSummary(
    // @PathVariable("clubUUID") UUID clubUUID) {
    // com.USWCicrcleLink.server.club.leader.dto.club.ClubSummaryResponse
    // clubSummaryResponse = clubLeaderService
    // .getClubSummary(clubUUID);
    // ApiResponse<com.USWCicrcleLink.server.club.leader.dto.club.ClubSummaryResponse>
    // response = new ApiResponse<>(
    // "동아리 요약 조회 완료", clubSummaryResponse);
    // return ResponseEntity.ok(response);
    // }

    // 지원서 상세 조회
    // @GetMapping("/{clubUUID}/applications/{applicationUUID}")
    // public
    // ResponseEntity<ApiResponse<com.USWCicrcleLink.server.club.application.dto.AplictDto.DetailResponse>>
    // getApplicationDetail(
    // @PathVariable("clubUUID") UUID clubUUID,
    // @PathVariable("applicationUUID") UUID applicationUUID) {
    // return ResponseEntity.ok(new ApiResponse<>("지원서 상세 조회 완료",
    // clubLeaderService.getApplicationDetail(clubUUID, applicationUUID)));
    // }

    // 지원자 상태 변경
    @PatchMapping("/{clubUUID}/applications/{applicationUUID}/status")
    public ResponseEntity<ApiResponse<Void>> updateApplicationStatus(
            @PathVariable("clubUUID") UUID clubUUID,
            @PathVariable("applicationUUID") UUID applicationUUID,
            @RequestBody @jakarta.validation.Valid com.USWCicrcleLink.server.club.application.dto.AplictDto.UpdateStatusRequest request) {
        clubLeaderService.updateAplictStatus(clubUUID, applicationUUID, request.getStatus());
        return ResponseEntity.ok(new ApiResponse<>("지원자 상태 변경 완료"));
    }

    // --- Added from FormController ---

    // 1️ 지원서 폼 생성
    @PostMapping("/{clubUUID}/forms")
    public ResponseEntity<ApiResponse<Void>> createForm(
            @PathVariable UUID clubUUID,
            @RequestBody @jakarta.validation.Valid com.USWCicrcleLink.server.club.leader.dto.FormDto.CreateRequest request) {
        formService.createForm(clubUUID, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>("폼 생성 성공"));
    }

    @GetMapping("/{clubUUID}/forms")
    public ResponseEntity<ApiResponse<ClubFormResponse>> getActiveForm(@PathVariable UUID clubUUID) {
        ClubFormResponse response = clubFormService.getQuestionsByClub(clubUUID);
        return ResponseEntity.ok(new ApiResponse<>("활성화된 지원서 조회 완료", response));
    }

}
