package com.USWCicrcleLink.server.club.controller;

import com.USWCicrcleLink.server.club.domain.Club;
import com.USWCicrcleLink.server.club.domain.Department;
import com.USWCicrcleLink.server.club.service.ClubService;
import com.USWCicrcleLink.server.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/clubs")
public class ClubController {
    private final ClubService clubService;

    //모든 동아리 조회
    @GetMapping
    public ResponseEntity<ApiResponse> getAllClubs() {
        List<Club> clubs = clubService.getAllClubs();
        ApiResponse response = new ApiResponse("모든 동아리 조회 성공", clubs);
        return ResponseEntity.ok(response);
    }

    //동아리 조회
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getClubById(@PathVariable("id") Long id) {
        Club club = clubService.getClubById(id);
        ApiResponse response = new ApiResponse("동아리 조회 성공", club);
        return ResponseEntity.ok(response);
    }

    //분과별 동아리 조회
    @GetMapping("/department/{department}")
    public ResponseEntity<ApiResponse> getClubsByDepartment(@PathVariable("department") Department department) {
        List<Club> clubs = clubService.getClubsByDepartment(department);
        ApiResponse response = new ApiResponse("분과별 동아리 조회 성공", clubs);
        return ResponseEntity.ok(response);
    }
}