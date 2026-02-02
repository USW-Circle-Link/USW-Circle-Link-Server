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
    public ResponseEntity<ApiResponse<Object>> uploadFloorMap(
            @RequestPart(value = "B1", required = false) MultipartFile b1,
            @RequestPart(value = "F1", required = false) MultipartFile f1,
            @RequestPart(value = "F2", required = false) MultipartFile f2) {

        java.util.List<FloorMapResponse> photoResponses = floorMapService.uploadFloorPhotos(b1, f1, f2);
        return ResponseEntity.ok(new ApiResponse<>("층별 사진 업로드 성공", photoResponses));
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
    @DeleteMapping("/{floorEnum}")
    public ResponseEntity<ApiResponse<String>> deleteFloorMap(
            @PathVariable("floorEnum") FloorPhotoEnum floorEnum) {
        floorMapService.deletePhotoByFloor(floorEnum);
        return ResponseEntity.ok(new ApiResponse<>("해당 층 사진 삭제 성공", "Floor: " + floorEnum.name()));
    }
}
