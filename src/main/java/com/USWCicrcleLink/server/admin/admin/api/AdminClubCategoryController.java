package com.USWCicrcleLink.server.admin.admin.api;

import com.USWCicrcleLink.server.admin.admin.dto.ClubCategoryCreationRequest;
import com.USWCicrcleLink.server.admin.admin.dto.ClubCategoryResponse;
import com.USWCicrcleLink.server.admin.admin.service.AdminClubCategoryService;
import com.USWCicrcleLink.server.club.club.domain.ClubCategory;
import com.USWCicrcleLink.server.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/category")
@RequiredArgsConstructor
public class AdminClubCategoryController {

    private final AdminClubCategoryService adminClubCategoryService;

    // 동아리 카테고리 설정(웹) - 카테고리 조회
    @GetMapping
    public ResponseEntity<ApiResponse<List<ClubCategoryResponse>>> getAllCategories() {
        List<ClubCategoryResponse> categories = adminClubCategoryService.getAllCategories();
        return ResponseEntity.ok(new ApiResponse<>("카테고리 리스트 조회 성공", categories));
    }

    // 동아리 카테고리 설정(웹) - 카테고리 추가
    @PostMapping
    public ResponseEntity<ApiResponse<ClubCategoryResponse>> addCategory(@RequestBody @Valid ClubCategoryCreationRequest request) {
        ClubCategoryResponse categoryDTO = adminClubCategoryService.addCategory(request);
        return ResponseEntity.ok(new ApiResponse<>("카테고리 추가 성공", categoryDTO));
    }


    // 동아리 카테고리 설정(웹) - 카테고리 삭제
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<ClubCategoryResponse>> deleteCategory(@PathVariable("categoryId") Long categoryId) {
        ClubCategoryResponse deletedCategory = adminClubCategoryService.deleteCategory(categoryId);
        ApiResponse<ClubCategoryResponse> response = new ApiResponse<>("카테고리 삭제 성공", deletedCategory);
        return ResponseEntity.ok(response);
    }
}
