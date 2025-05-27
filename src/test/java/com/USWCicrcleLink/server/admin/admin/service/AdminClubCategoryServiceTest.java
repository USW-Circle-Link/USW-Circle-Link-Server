package com.USWCicrcleLink.server.admin.admin.service;

import com.USWCicrcleLink.server.admin.admin.dto.AdminClubCategoryCreationRequest;
import com.USWCicrcleLink.server.admin.admin.mapper.ClubCategoryMapper;
import com.USWCicrcleLink.server.club.club.domain.ClubCategory;
import com.USWCicrcleLink.server.club.club.dto.ClubCategoryResponse;
import com.USWCicrcleLink.server.club.club.repository.ClubCategoryMappingRepository;
import com.USWCicrcleLink.server.club.club.repository.ClubCategoryRepository;
import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.ClubCategoryException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminClubCategoryServiceTest {

    @Mock
    private ClubCategoryRepository clubCategoryRepository;
    @Mock
    private ClubCategoryMappingRepository clubCategoryMappingRepository;

    @InjectMocks
    private AdminClubCategoryService adminClubCategoryService;

    @Test
    @DisplayName("카테고리 생성")
    void testAddClubCategorySuccess() {
        // given
        AdminClubCategoryCreationRequest request = new AdminClubCategoryCreationRequest("Art");
        ClubCategory saved = ClubCategory.builder()
                .clubCategoryId(1L)
                .clubCategoryName("art")
                .build();
        ClubCategoryResponse mockResponse = new ClubCategoryResponse(UUID.randomUUID(), "art");

        when(clubCategoryRepository.existsByClubCategoryName("art")).thenReturn(false);
        when(clubCategoryRepository.save(any())).thenReturn(saved);

        try (MockedStatic<ClubCategoryMapper> mocked = mockStatic(ClubCategoryMapper.class)) {
            mocked.when(() -> ClubCategoryMapper.toDto(saved)).thenReturn(mockResponse);

            // when
            ClubCategoryResponse result = adminClubCategoryService.addClubCategory(request);

            // then
            assertThat(result.getClubCategoryName()).isEqualTo("art");
            verify(clubCategoryRepository).save(any());
        }
    }

    @Test
    @DisplayName("카테고리 이름 중복 예외 발생")
    void testAddClubCategoryDuplicate() {
        AdminClubCategoryCreationRequest request = new AdminClubCategoryCreationRequest("Art");
        when(clubCategoryRepository.existsByClubCategoryName("art")).thenReturn(true);

        assertThatThrownBy(() -> adminClubCategoryService.addClubCategory(request))
                .isInstanceOf(ClubCategoryException.class)
                .hasMessageContaining(ExceptionType.DUPLICATE_CATEGORY.getMessage());
    }

    @Test
    @DisplayName("카테고리 삭제")
    void testDeleteClubCategorySuccess() {
        UUID uuid = UUID.randomUUID();
        ClubCategory entity = ClubCategory.builder()
                .clubCategoryId(1L)
                .clubCategoryUUID(uuid)
                .clubCategoryName("sports")
                .build();
        ClubCategoryResponse expected = new ClubCategoryResponse(uuid, "sports");

        when(clubCategoryRepository.findByClubCategoryUUID(uuid)).thenReturn(Optional.of(entity));

        try (MockedStatic<ClubCategoryMapper> mocked = mockStatic(ClubCategoryMapper.class)) {
            mocked.when(() -> ClubCategoryMapper.toDto(entity)).thenReturn(expected);

            ClubCategoryResponse result = adminClubCategoryService.deleteClubCategory(uuid);

            assertThat(result.getClubCategoryName()).isEqualTo("sports");
            verify(clubCategoryMappingRepository).deleteByClubCategoryId(1L);
            verify(clubCategoryRepository).delete(entity);
        }
    }

    @Test
    @DisplayName("UUID 존재하지 않음 예외")
    void testDeleteClubCategoryNotFound() {
        UUID uuid = UUID.randomUUID();
        when(clubCategoryRepository.findByClubCategoryUUID(uuid)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminClubCategoryService.deleteClubCategory(uuid))
                .isInstanceOf(ClubCategoryException.class)
                .hasMessageContaining(ExceptionType.CATEGORY_NOT_FOUND.getMessage());
    }
}
