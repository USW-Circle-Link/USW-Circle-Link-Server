package com.USWCicrcleLink.server.category.service;

import com.USWCicrcleLink.server.category.dto.CategoryRequest;
import com.USWCicrcleLink.server.category.mapper.ClubCategoryMapper;
import com.USWCicrcleLink.server.category.dto.ClubCategoryDto;
import com.USWCicrcleLink.server.category.domain.ClubCategory;
import com.USWCicrcleLink.server.category.repository.ClubCategoryMappingRepository;
import com.USWCicrcleLink.server.category.repository.ClubCategoryRepository;
import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.BaseException;
import com.USWCicrcleLink.server.global.exception.errortype.ClubCategoryException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final ClubCategoryRepository clubCategoryRepository;
    private final ClubCategoryMappingRepository clubCategoryMappingRepository;

    /**
     * 동아리 카테고리 설정(ADMIN, LEADER) - 카테고리 조회
     */
    @Transactional(readOnly = true)
    public List<ClubCategoryDto> getAllClubCategories() {
        List<ClubCategory> clubCategories = clubCategoryRepository.findAll();
        log.debug("동아리 카테고리 조회 성공 - {}개 카테고리 반환", clubCategories.size());

        return ClubCategoryMapper.toDtoList(clubCategories);
    }

    /**
     * 동아리 카테고리 설정(ADMIN) - 카테고리 추가
     */
    public ClubCategoryDto addClubCategory(CategoryRequest request) {
        String normalizedCategoryName = request.getClubCategoryName().toLowerCase();

        if (clubCategoryRepository.existsByClubCategoryName(normalizedCategoryName)) {
            throw new ClubCategoryException(ExceptionType.DUPLICATE_CATEGORY);
        }

        ClubCategory clubCategory = ClubCategory.builder()
                .clubCategoryName(normalizedCategoryName)
                .build();

        ClubCategory savedClubCategory = clubCategoryRepository.save(clubCategory);
        log.info("동아리 카테고리 추가 성공 - ID: {}, Name: {}", savedClubCategory.getClubCategoryId(),
                savedClubCategory.getClubCategoryName());

        return ClubCategoryMapper.toDto(savedClubCategory);
    }

    /**
     * 동아리 카테고리 설정(ADMIN) - 카테고리 삭제
     */
    public ClubCategoryDto deleteClubCategory(UUID clubCategoryUUID) {
        ClubCategory clubCategory = clubCategoryRepository.findByClubCategoryUUID(clubCategoryUUID)
                .orElseThrow(() -> new ClubCategoryException(ExceptionType.CATEGORY_NOT_FOUND));

        try {
            clubCategoryMappingRepository.deleteByClubCategoryId(clubCategory.getClubCategoryId());
            clubCategoryRepository.delete(clubCategory);
            log.info("동아리 카테고리 삭제 성공 - ID: {}", clubCategory.getClubCategoryId());
        } catch (Exception e) {
            log.error("카테고리 삭제 중 예외 발생 - UUID: {}, 오류: {}", clubCategoryUUID, e.getMessage());
            throw new BaseException(ExceptionType.SERVER_ERROR, e);
        }

        return ClubCategoryMapper.toDto(clubCategory);
    }

}
