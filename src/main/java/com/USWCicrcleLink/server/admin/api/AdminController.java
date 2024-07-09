package com.USWCicrcleLink.server.admin.api;

import com.USWCicrcleLink.server.admin.dto.ClubCreationRequest;
import com.USWCicrcleLink.server.admin.dto.ClubDetailDto;
import com.USWCicrcleLink.server.admin.service.AdminService;
import com.USWCicrcleLink.server.club.domain.Club;
import com.USWCicrcleLink.server.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

    //동아리 전체 리스트 조회
    @GetMapping("/clubs")
    public ResponseEntity<ApiResponse<List<Club>>> getAllClubs() {
        List<Club> clubs = adminService.getAllClubs();
        ApiResponse<List<Club>> response = new ApiResponse<>("동아리 리스트 조회 성공", clubs);
        return ResponseEntity.ok(response);
    }

    //동아리 페이징 리스트 조회
    @GetMapping("/clubs/paged")
    public ResponseEntity<ApiResponse<Page<Club>>> getClubs(Pageable pageable) {
        Page<Club> clubs = adminService.getClubs(pageable);
        ApiResponse<Page<Club>> response = new ApiResponse<>("동아리 페이징 리스트 조회 성공", clubs);
        return ResponseEntity.ok(response);
    }

    //동아리 상세 페이지 조회
    @GetMapping("/clubs/{clubId}")
    public ResponseEntity<ApiResponse<ClubDetailDto>> getClubById(@PathVariable Long clubId) {
        ClubDetailDto clubDetailDto = adminService.getClubById(clubId);
        ApiResponse<ClubDetailDto> response = new ApiResponse<>("동아리 상세 조회 성공", clubDetailDto);
        return ResponseEntity.ok(response);
    }

    //동아리 생성
    @PostMapping("/club/create")
    public ResponseEntity<ApiResponse<Club>> createClub(@RequestBody ClubCreationRequest request) {
        adminService.createClub(request);
        ApiResponse<Club> response = new ApiResponse<>("동아리 생성 성공", null);
        return ResponseEntity.ok(response);
    }

    //동아리 삭제
    @DeleteMapping("/club/delete/{clubId}")
    public ResponseEntity<ApiResponse<Long>> deleteClub(@PathVariable Long clubId, @RequestBody String adminPassword) {
        adminService.deleteClub(clubId, adminPassword);
        ApiResponse<Long> response = new ApiResponse<>("동아리 삭제 성공", clubId);
        return ResponseEntity.ok(response);
    }
}
