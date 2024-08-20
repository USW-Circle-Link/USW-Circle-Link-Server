package com.USWCicrcleLink.server.clubLeader.api;

import com.USWCicrcleLink.server.aplict.dto.ApplicantResultsRequest;
import com.USWCicrcleLink.server.club.clubIntro.dto.ClubIntroResponse;
import com.USWCicrcleLink.server.clubLeader.dto.*;
import com.USWCicrcleLink.server.clubLeader.service.ClubLeaderService;
import com.USWCicrcleLink.server.clubLeader.service.FcmServiceImpl;
import com.USWCicrcleLink.server.global.response.ApiResponse;
import com.USWCicrcleLink.server.global.response.PageResponse;
import com.USWCicrcleLink.server.global.util.s3File.Service.S3FileUploadService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/club-leader")
@Slf4j
public class ClubLeaderController {

    private final ClubLeaderService clubLeaderService;
    private final FcmServiceImpl fcmService;
    private final S3FileUploadService fileUploadService;

    // 동아리 기본 정보 조회
    @GetMapping("/{clubId}/info")
    public ResponseEntity<ApiResponse> getClubInfo(@PathVariable("clubId") Long clubId) {
        ApiResponse<ClubInfoResponse> clubInfo = clubLeaderService.getClubInfo(clubId);
        return new ResponseEntity<>(clubInfo, HttpStatus.OK);
    }

    // 동아리 기본 정보 변경
    @PatchMapping("/{clubId}/info")
    public ResponseEntity<ApiResponse> updateClubInfo(@PathVariable("clubId") Long clubId,
                                                      @RequestPart(value = "mainPhoto", required = true) MultipartFile mainPhoto,
                                                      @RequestPart(value = "clubInfoRequest", required = false) ClubInfoRequest clubInfoRequest) throws IOException {

        return new ResponseEntity<>(clubLeaderService.updateClubInfo(clubId, clubInfoRequest, mainPhoto), HttpStatus.OK);
    }

    // 동아리 소개 조회
    @GetMapping("/{clubId}/intro")
    public ResponseEntity<ApiResponse<ClubIntroResponse>> getClubById(@PathVariable("clubId") Long clubId) {
        ClubIntroResponse clubIntroResponse = clubLeaderService.getClubIntro(clubId);
        ApiResponse<ClubIntroResponse> response = new ApiResponse<>("동아리 상세 조회 성공", clubIntroResponse);
        return ResponseEntity.ok(response);
    }

    // 동아리 소개 변경
    @PatchMapping("/{clubId}/intro")
    public ResponseEntity<ApiResponse> updateClubIntro(@PathVariable("clubId") Long clubId,
                                                       @RequestPart(value = "clubIntroRequest", required = false) ClubIntroRequest clubIntroRequest,
                                                       @RequestPart(value = "introPhotos", required = false) List<MultipartFile> introPhotos) throws IOException {

        return new ResponseEntity<>(clubLeaderService.updateClubIntro(clubId, clubIntroRequest, introPhotos), HttpStatus.OK);
    }

    // 동아리 모집 상태 변경
    @PatchMapping("/{clubId}/toggle-recruitment")
    public ResponseEntity<ApiResponse> toggleRecruitmentStatus(@PathVariable("clubId") Long clubId) {
        return new ResponseEntity<>(clubLeaderService.toggleRecruitmentStatus(clubId), HttpStatus.OK);
    }

//    @GetMapping("/v1/members")
//    public ResponseEntity<ApiResponse> getClubMembers(LeaderToken token) {
//        // 원래는 GET 요청임 토큰때문
//        return new ResponseEntity<>(clubLeaderService.findClubMembers(token), HttpStatus.OK);
//    }

    // 소속 동아리원 조회
    @GetMapping("/{clubId}/members")
    public ResponseEntity<ApiResponse> getClubMembers(@PathVariable("clubId") Long clubId, @RequestParam("page") int page, @RequestParam("size") int size) {
        return new ResponseEntity<>(clubLeaderService.getClubMembers(clubId, page, size), HttpStatus.OK);
    }

    // 동아리원 퇴출
    @DeleteMapping("/{clubId}/members/{clubMemberId}")
    public ResponseEntity<ApiResponse> deleteClubMember(@PathVariable("clubMemberId") Long clubMemberId, @PathVariable("clubId") Long clubId) {
        return new ResponseEntity<>(clubLeaderService.deleteClubMember(clubMemberId, clubId), HttpStatus.OK);
    }

    // 동아리원 엑셀 파일 추출
    @GetMapping("/{clubId}/members/export")
    public void exportClubMembers(@PathVariable("clubId") Long clubId, HttpServletResponse response) {
        // 엑셀 파일 생성
        clubLeaderService.downloadExcel(clubId, response);
    }

    // 최초 지원자 조회
    @GetMapping("/{clubId}/applicants")
    public ResponseEntity<ApiResponse> getApplicants(@PathVariable("clubId") Long clubId, @RequestParam("page") int page, @RequestParam("size") int size) {
        return new ResponseEntity<>(clubLeaderService.getApplicants(clubId, page, size), HttpStatus.OK);
    }

    // 최초 합격자 알림
    @PostMapping("/{clubId}/applicants/notifyMultiple")
    public ResponseEntity<ApiResponse> pushApplicantResults(@PathVariable("clubId") Long clubId, @RequestBody List<ApplicantResultsRequest> results) throws IOException {
        clubLeaderService.updateApplicantResults(clubId, results);
        return new ResponseEntity<>(new ApiResponse<>("지원 결과 처리 완료"), HttpStatus.OK);
    }

    // 불합격자 조회
    @GetMapping("/{clubId}/failed-applicants")
    public ApiResponse<PageResponse> getFailedApplicants(@PathVariable("clubId") Long clubId, @RequestParam("page") int page, @RequestParam("size") int size) {
        return clubLeaderService.getFailedApplicants(clubId, page, size);
    }

    // 지원자 추가 합격 알림
    @PostMapping("/{clubId}/failed-applicants/notifyMultiple")
    public ResponseEntity<ApiResponse> pushFailedApplicantResults(@PathVariable("clubId") Long clubId, @RequestBody List<ApplicantResultsRequest> results) throws IOException {
        clubLeaderService.updateFailedApplicantResults(clubId, results);
        return new ResponseEntity<>(new ApiResponse<>("추합 결과 처리 완료"), HttpStatus.OK);
    }

}
