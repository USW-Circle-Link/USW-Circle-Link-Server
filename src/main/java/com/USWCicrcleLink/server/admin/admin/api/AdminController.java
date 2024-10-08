package com.USWCicrcleLink.server.admin.admin.api;

import com.USWCicrcleLink.server.admin.admin.dto.*;
import com.USWCicrcleLink.server.admin.admin.service.AdminService;
import com.USWCicrcleLink.server.club.clubIntro.dto.ClubIntroResponse;
import com.USWCicrcleLink.server.club.clubIntro.service.ClubIntroService;
import com.USWCicrcleLink.server.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;
    private final ClubIntroService clubIntroService;

    // 동아리 전체 리스트 조회(웹)
    @GetMapping("/clubs")
    public ResponseEntity<ApiResponse<List<ClubAdminListResponse>>> getAllClubs() {
        List<ClubAdminListResponse> clubs = adminService.getAllClubs();
        ApiResponse<List<ClubAdminListResponse>> response = new ApiResponse<>("동아리 전체 리스트 조회 성공", clubs);
        return ResponseEntity.ok(response);
    }

    // 동아리 상세 페이지 조회(웹)
    @GetMapping("/clubs/{clubId}")
    public ResponseEntity<ApiResponse<ClubIntroResponse>> getClubById(@PathVariable("clubId") Long clubId) {
        ClubIntroResponse clubIntroResponse = clubIntroService.getClubIntro(clubId);
        ApiResponse<ClubIntroResponse> response = new ApiResponse<>("동아리 상세 조회 성공", clubIntroResponse);
        return ResponseEntity.ok(response);
    }

    // 동아리 생성(웹)
    @PostMapping("/clubs")
    public ResponseEntity<ApiResponse<ClubCreationResponse>> createClub(@RequestBody @Valid ClubCreationRequest clubRequest) {
        ClubCreationResponse clubCreationResponse = adminService.createClub(clubRequest);
        ApiResponse<ClubCreationResponse> response = new ApiResponse<>("동아리 생성 성공", clubCreationResponse);
        return ResponseEntity.ok(response);
    }

    // 동아리 삭제(웹)
    @DeleteMapping("/clubs/{clubId}")
    public ResponseEntity<ApiResponse<Long>> deleteClub(@PathVariable("clubId") Long clubId, @RequestBody @Valid AdminPwRequest pwRequest) {
        adminService.deleteClub(clubId, pwRequest.getAdminPw());
        ApiResponse<Long> response = new ApiResponse<>("동아리 삭제 성공: clubId", clubId);
        return ResponseEntity.ok(response);
    }
}
