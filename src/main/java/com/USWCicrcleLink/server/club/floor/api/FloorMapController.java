package com.USWCicrcleLink.server.club.floor.api;

import com.USWCicrcleLink.server.club.floor.dto.FloorMapResponse;
import com.USWCicrcleLink.server.club.floor.service.FloorMapService;
import com.USWCicrcleLink.server.club.domain.FloorPhotoEnum;
import com.USWCicrcleLink.server.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/floor-maps")
@RequiredArgsConstructor
public class FloorMapController {

    private final FloorMapService floorMapService;

    // 동아리 위치 정보 수정 - 층별 사진 업로드 (ADMIN)
    @PutMapping
    public ResponseEntity<ApiResponse<FloorMapResponse>> uploadFloorMap(
            @RequestParam("floor") FloorPhotoEnum floor,
            @RequestPart("photo") MultipartFile photo) {
        FloorMapResponse photoResponse = floorMapService.uploadPhoto(floor, photo);
        return ResponseEntity.ok(new ApiResponse<>("해당 층 사진 업로드 성공", photoResponse));
    }

    // 동아리 위치 정보 조회 (ALL)
    @GetMapping
    public ResponseEntity<ApiResponse<Object>> getFloorMaps(
            @RequestParam(value = "floor", required = false) FloorPhotoEnum floor) {
        if (floor != null) {
            return ResponseEntity.ok(new ApiResponse<>("해당 층 사진 조회 성공", floorMapService.getFloorMap(floor)));
        }
        return ResponseEntity.ok(new ApiResponse<>("전체 층 사진 조회 성공", floorMapService.getAllFloorMaps()));
    }

    // 동아리 위치 정보 수정 - 특정 층 사진 삭제 (ADMIN)
    @DeleteMapping("/{floorUUId}")
    public ResponseEntity<ApiResponse<String>> deleteFloorMap(
            @PathVariable("floorUUId") FloorPhotoEnum floor) {
        floorMapService.deletePhotoByFloor(floor);
        return ResponseEntity.ok(new ApiResponse<>("해당 층 사진 삭제 성공", "Floor: " + floor.name()));
    }
}


