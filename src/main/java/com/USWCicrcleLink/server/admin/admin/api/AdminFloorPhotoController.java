package com.USWCicrcleLink.server.admin.admin.api;

import com.USWCicrcleLink.server.admin.admin.dto.FloorPhotoCreationResponse;
import com.USWCicrcleLink.server.admin.admin.service.AdminFloorPhotoService;
import com.USWCicrcleLink.server.club.club.domain.FloorPhotoEnum;
import com.USWCicrcleLink.server.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/admin/floor/photo")
@RequiredArgsConstructor
public class AdminFloorPhotoController {

    private final AdminFloorPhotoService adminFloorPhotoService;

    // 동아리 위치 정보 수정(웹) - 층별 사진 업로드
    @PutMapping("/{floor}")
    public ResponseEntity<ApiResponse<FloorPhotoCreationResponse>> uploadFloorPhoto(
            @PathVariable("floor") FloorPhotoEnum floor,
            @RequestPart("photo") MultipartFile photo) {
        FloorPhotoCreationResponse photoResponse = adminFloorPhotoService.uploadPhoto(floor, photo);
        ApiResponse<FloorPhotoCreationResponse> response = new ApiResponse<>("해당 층 사진 업로드 성공", photoResponse);
        return ResponseEntity.ok(response);
    }

    // 동아리 위치 정보 수정(웹) - 특정 층의 사진 조회
    @GetMapping("/{floor}")
    public ResponseEntity<ApiResponse<FloorPhotoCreationResponse>> getPhotoByFloor(
            @PathVariable("floor") FloorPhotoEnum floor) {
        FloorPhotoCreationResponse photoResponse = adminFloorPhotoService.getPhotoByFloor(floor);
        ApiResponse<FloorPhotoCreationResponse> response = new ApiResponse<>("해당 층 사진 조회 성공", photoResponse);
        return ResponseEntity.ok(response);
    }

    // 동아리 위치 정보 수정(웹) - 특정 층 사진 삭제
    @DeleteMapping("/{floor}")
    public ResponseEntity<ApiResponse<String>> deletePhotoByFloor(
            @PathVariable("floor") FloorPhotoEnum floor) {
        adminFloorPhotoService.deletePhotoByFloor(floor);
        ApiResponse<String> response = new ApiResponse<>("해당 층 사진 삭제 성공", "Floor: " + floor.name());
        return ResponseEntity.ok(response);
    }
}
