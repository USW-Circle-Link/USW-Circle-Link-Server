package com.USWCicrcleLink.server.category.mapper;

import com.USWCicrcleLink.server.category.dto.ClubCategoryDto;
import com.USWCicrcleLink.server.category.domain.ClubCategory;

import java.util.List;
import java.util.stream.Collectors;

public class ClubCategoryMapper {

    // 단일 객체 변환 (Entity → DTO)
    public static ClubCategoryDto toDto(ClubCategory clubCategory) {
        return new ClubCategoryDto(
                clubCategory.getClubCategoryUUID(),
                clubCategory.getClubCategoryName());
    }

    // 리스트 변환 (List<Entity> → List<DTO>)
    public static List<ClubCategoryDto> toDtoList(List<ClubCategory> categories) {
        return categories.stream()
                .map(ClubCategoryMapper::toDto)
                .collect(Collectors.toList());
    }
}
