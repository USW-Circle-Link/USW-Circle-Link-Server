package com.USWCicrcleLink.server.category.api;

import com.USWCicrcleLink.server.category.dto.CategoryRequest;
import com.USWCicrcleLink.server.category.service.CategoryService;
import com.USWCicrcleLink.server.club.dto.ClubCategoryDto;
import com.USWCicrcleLink.server.global.response.ApiResponse;
import com.USWCicrcleLink.server.global.validation.ValidationSequence;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    // 동아리 카테고리 설정 - 카테고리 조회
    @GetMapping
    public ResponseEntity<ApiResponse<List<ClubCategoryDto>>> getAllClubCategories() {
        List<ClubCategoryDto> clubCategories = categoryService.getAllClubCategories();
        return ResponseEntity.ok(new ApiResponse<>("카테고리 리스트 조회 성공", clubCategories));
    }

    // 동아리 카테고리 설정 - 카테고리 추가
    @PostMapping
    public ResponseEntity<ApiResponse<ClubCategoryDto>> addClubCategory(
            @RequestBody @Validated(ValidationSequence.class) CategoryRequest request) {
        ClubCategoryDto addedClubCategory = categoryService.addClubCategory(request);
        return ResponseEntity.ok(new ApiResponse<>("카테고리 추가 성공", addedClubCategory));
    }

    // 동아리 카테고리 설정 - 카테고리 삭제
    @DeleteMapping("/{clubCategoryUUID}")
    public ResponseEntity<ApiResponse<ClubCategoryDto>> deleteClubCategory(
            @PathVariable("clubCategoryUUID") UUID clubCategoryUUID) {
        ClubCategoryDto deletedCategory = categoryService.deleteClubCategory(clubCategoryUUID);
        return ResponseEntity.ok(new ApiResponse<>("카테고리 삭제 성공", deletedCategory));
    }
}
