package com.USWCicrcleLink.server.admin.admin.api;

import com.USWCicrcleLink.server.admin.admin.dto.AdminFloorPhotoCreationResponse;
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

    // 동아리 위치 정보 수정 - 층별 사진 업로드 (ADMIN)
    @PutMapping("/{floor}")
    public ResponseEntity<ApiResponse<AdminFloorPhotoCreationResponse>> uploadFloorPhoto(
            @PathVariable("floor") FloorPhotoEnum floor,
            @RequestPart("photo") MultipartFile photo) {
        AdminFloorPhotoCreationResponse photoResponse = adminFloorPhotoService.uploadPhoto(floor, photo);
        return ResponseEntity.ok(new ApiResponse<>("해당 층 사진 업로드 성공", photoResponse));
    }

    // 동아리 위치 정보 수정 - 특정 층의 사진 조회 (ADMIN)
    @GetMapping("/{floor}")
    public ResponseEntity<ApiResponse<AdminFloorPhotoCreationResponse>> getPhotoByFloor(
            @PathVariable("floor") FloorPhotoEnum floor) {
        AdminFloorPhotoCreationResponse photoResponse = adminFloorPhotoService.getPhotoByFloor(floor);
        return ResponseEntity.ok(new ApiResponse<>("해당 층 사진 조회 성공", photoResponse));
    }

    // 동아리 위치 정보 수정 - 특정 층 사진 삭제 (ADMIN)
    @DeleteMapping("/{floor}")
    public ResponseEntity<ApiResponse<String>> deletePhotoByFloor(
            @PathVariable("floor") FloorPhotoEnum floor) {
        adminFloorPhotoService.deletePhotoByFloor(floor);
        return ResponseEntity.ok(new ApiResponse<>("해당 층 사진 삭제 성공", "Floor: " + floor.name()));
    }
}
