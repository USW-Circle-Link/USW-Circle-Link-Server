package com.USWCicrcleLink.server.admin.api;

import com.USWCicrcleLink.server.admin.dto.AdminClubCreationRequest;
import com.USWCicrcleLink.server.admin.dto.AdminClubIntroResponse;
import com.USWCicrcleLink.server.admin.dto.AdminClubPageListResponse;
import com.USWCicrcleLink.server.admin.dto.AdminPwRequest;
import com.USWCicrcleLink.server.admin.service.AdminClubService;
import com.USWCicrcleLink.server.club.service.ClubService;
import com.USWCicrcleLink.server.global.response.ApiResponse;
import com.USWCicrcleLink.server.global.validation.ValidationSequence;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin/clubs")
@RequiredArgsConstructor
public class AdminClubController {

    private final AdminClubService adminClubService;
    private final ClubService clubService;

    // 동아리 추가 - 동아리 추가 (ADMIN)
    @PostMapping
    public ResponseEntity<ApiResponse<String>> createClub(
            @RequestBody @Validated(ValidationSequence.class) AdminClubCreationRequest clubRequest) {
        adminClubService.createClub(clubRequest);
        return ResponseEntity.ok(new ApiResponse<>("동아리 생성 성공"));
    }

    // 동아리 삭제 (ADMIN)
    @DeleteMapping("{clubUUID}")
    public ResponseEntity<ApiResponse<Long>> deleteClub(@PathVariable("clubUUID") UUID clubUUID,
            @RequestBody @Validated(ValidationSequence.class) AdminPwRequest request) {
        adminClubService.deleteClub(clubUUID, request);
        return ResponseEntity.ok(new ApiResponse<>("동아리 삭제 성공"));
    }

    // 동아리 추가 - 동아리 회장 아이디 중복 확인 (ADMIN)
    @GetMapping("/leader/check")
    public ResponseEntity<ApiResponse<String>> checkLeaderAccountDuplicate(
            @RequestParam("leaderAccount") String leaderAccount) {
        adminClubService.validateLeaderAccount(leaderAccount);
        return ResponseEntity.ok(new ApiResponse<>("사용 가능한 동아리 회장 아이디입니다."));
    }

    // 동아리 추가 - 동아리 이름 중복 확인 (ADMIN)
    @GetMapping("/name/check")
    public ResponseEntity<ApiResponse<String>> checkClubNameDuplicate(
            @RequestParam("clubName") String clubName) {
        adminClubService.validateClubName(clubName);
        return ResponseEntity.ok(new ApiResponse<>("사용 가능한 동아리 이름입니다."));
    }
}
