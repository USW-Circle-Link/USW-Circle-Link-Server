package com.USWCicrcleLink.server.club.service;

import com.USWCicrcleLink.server.admin.dto.AdminClubIntroResponse;
import com.USWCicrcleLink.server.category.mapper.ClubCategoryMapper;
import com.USWCicrcleLink.server.club.domain.Club;
import com.USWCicrcleLink.server.category.domain.ClubCategory;
import com.USWCicrcleLink.server.club.domain.RecruitmentStatus;
import com.USWCicrcleLink.server.club.domain.ClubHashtag;
import com.USWCicrcleLink.server.club.domain.ClubHashtag;
import com.USWCicrcleLink.server.club.dto.ClubListResponse;
import com.USWCicrcleLink.server.club.dto.ClubSearchCondition;
import com.USWCicrcleLink.server.category.dto.ClubCategoryDto;
import com.USWCicrcleLink.server.club.repository.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.USWCicrcleLink.server.category.repository.ClubCategoryRepository;
import com.USWCicrcleLink.server.category.repository.ClubCategoryMappingRepository;
import com.USWCicrcleLink.server.club.clubIntro.domain.ClubIntro;
import com.USWCicrcleLink.server.club.clubIntro.domain.ClubIntroPhoto;
import com.USWCicrcleLink.server.club.clubIntro.repository.ClubIntroPhotoRepository;
import com.USWCicrcleLink.server.club.clubIntro.repository.ClubIntroRepository;
import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.BaseException;
import com.USWCicrcleLink.server.global.exception.errortype.ClubException;
import com.USWCicrcleLink.server.global.s3File.Service.S3FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ClubService {

        private final ClubCategoryMappingRepository clubCategoryMappingRepository;
        private final ClubCategoryRepository clubCategoryRepository;
        private final ClubMainPhotoRepository clubMainPhotoRepository;
        private final ClubHashtagRepository clubHashtagRepository;
        private final S3FileUploadService s3FileUploadService;
        private final ClubIntroRepository clubIntroRepository;
        private final ClubRepository clubRepository;
        private final ClubIntroPhotoRepository clubIntroPhotoRepository;
        private final ClubMembersRepository clubMembersRepository;

        // 동아리 검색 (필터링, 조건부 필드 포함)
        @Transactional(readOnly = true)
        public List<ClubListResponse> searchClubs(ClubSearchCondition condition) {
                // 1. 카테고리 개수 검증
                if (condition.getCategoryUUIDs() != null && condition.getCategoryUUIDs().size() > 3) {
                        throw new BaseException(ExceptionType.INVALID_CATEGORY_COUNT);
                }

                // 2. 동아리 ID 검색
                List<Long> clubIds = clubRepository.searchClubIds(condition);

                if (clubIds.isEmpty()) {
                        return Collections.emptyList();
                }

                // 3. 동아리 정보 조회
                List<Club> clubs = clubRepository.findByClubIds(clubIds);

                // 4. 연관 데이터 조회 (메인 사진, 해시태그, 모집 상태)
                Map<Long, String> mainPhotoUrls = getMainPhotoUrls(clubIds);
                Map<Long, List<String>> clubHashtags = getClubHashtags(clubIds);
                Map<Long, String> recruitmentStatuses = getRecruitmentStatusMap(clubIds);

                // 5. 회원 수 조회 (권한 확인)
                Map<Long, Long> memberCounts = getMemberCountsIfAuthorized(clubIds,
                                condition.getIncludeNumberOfMembers());

                // 6. DTO 변환
                return clubs.stream()
                                .map(club -> new ClubListResponse(
                                                club.getClubuuid(),
                                                club.getClubName(),
                                                mainPhotoUrls.getOrDefault(club.getClubId(), null),
                                                club.getDepartment().name(),
                                                clubHashtags.getOrDefault(club.getClubId(), Collections.emptyList()),
                                                club.getLeaderName(),
                                                memberCounts.isEmpty() ? null
                                                                : memberCounts.getOrDefault(club.getClubId(), 0L),
                                                recruitmentStatuses.getOrDefault(club.getClubId(),
                                                                RecruitmentStatus.CLOSE.name())))
                                .collect(Collectors.toList());
        }

        private Map<Long, String> getMainPhotoUrls(List<Long> clubIds) {
                return clubMainPhotoRepository.findByClubIds(clubIds)
                                .stream()
                                .collect(Collectors.toMap(
                                                photo -> photo.getClub().getClubId(),
                                                photo -> s3FileUploadService.generatePresignedGetUrl(
                                                                photo.getClubMainPhotoS3Key())));
        }

        private Map<Long, List<String>> getClubHashtags(List<Long> clubIds) {
                return clubHashtagRepository.findByClubIds(clubIds)
                                .stream()
                                .collect(Collectors.groupingBy(
                                                tag -> tag.getClub().getClubId(),
                                                Collectors.mapping(ClubHashtag::getClubHashtag, Collectors.toList())));
        }

        private Map<Long, Long> getMemberCountsIfAuthorized(List<Long> clubIds, Boolean includeNumberOfMembers) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.getAuthorities().stream()
                                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")
                                                || a.getAuthority().equals("ROLE_UNION"))) {
                        return getMemberCountsMap(clubIds);
                }
                return Collections.emptyMap();
        }

        // 카테고리 조회
        @Transactional(readOnly = true)
        public List<ClubCategoryDto> getAllClubCategories() {
                List<ClubCategory> clubCategories = clubCategoryRepository.findAll();
                return ClubCategoryMapper.toDtoList(clubCategories);
        }

        // 동아리 소개/모집글 페이지 조회 (웹 - 운영팀, 모바일)
        @Transactional(readOnly = true)
        public AdminClubIntroResponse getClubIntro(UUID clubUUID) {
                Club club = clubRepository.findByClubuuid(clubUUID)
                                .orElseThrow(() -> new ClubException(ExceptionType.CLUB_NOT_EXISTS));

                Long clubId = club.getClubId();

                ClubIntro clubIntro = clubIntroRepository.findByClubClubId(clubId)
                                .orElseThrow(() -> new ClubException(ExceptionType.CLUB_INTRO_NOT_EXISTS));

                String mainPhotoUrl = clubMainPhotoRepository.findByClubClubId(clubId)
                                .map(photo -> s3FileUploadService
                                                .generatePresignedGetUrl(photo.getClubMainPhotoS3Key()))
                                .orElse(null);

                List<String> introPhotoUrls = clubIntroPhotoRepository.findByClubIntroClubId(clubId)
                                .stream()
                                .sorted(Comparator.comparingInt(ClubIntroPhoto::getOrder))
                                .map(photo -> s3FileUploadService
                                                .generatePresignedGetUrl(photo.getClubIntroPhotoS3Key()))
                                .collect(Collectors.toList());

                List<String> hashtags = clubHashtagRepository.findByClubClubId(clubId)
                                .stream()
                                .map(ClubHashtag::getClubHashtag)
                                .collect(Collectors.toList());

                List<String> clubCategoryNames = clubCategoryMappingRepository.findByClubClubId(clubId)
                                .stream()
                                .map(mapping -> mapping.getClubCategory().getClubCategoryName())
                                .collect(Collectors.toList());

                return new AdminClubIntroResponse(
                                club.getClubuuid(),
                                mainPhotoUrl,
                                introPhotoUrls,
                                club.getClubName(),
                                club.getLeaderName(),
                                club.getLeaderHp(),
                                club.getClubInsta(),
                                clubIntro.getClubIntro(),
                                clubIntro.getRecruitmentStatus(),
                                clubIntro.getGoogleFormUrl(),
                                hashtags,
                                clubCategoryNames,
                                club.getClubRoomNumber(),
                                clubIntro.getClubRecruitment());
        }
        // List<Long> clubIds check necessary?
        // Yes, but I'll assume caller handles emptiness or it just returns empty map.

        private Map<Long, Long> getMemberCountsMap(List<Long> clubIds) {
                if (clubIds == null || clubIds.isEmpty()) {
                        return Collections.emptyMap();
                }
                return clubMembersRepository.countMembersByClubIds(clubIds).stream()
                                .collect(Collectors.toMap(
                                                result -> (Long) result[0],
                                                result -> (Long) result[1]));
        }

        private Map<Long, String> getRecruitmentStatusMap(List<Long> clubIds) {
                if (clubIds == null || clubIds.isEmpty()) {
                        return Collections.emptyMap();
                }
                return clubIntroRepository.findRecruitmentStatusByClubIds(clubIds).stream()
                                .collect(Collectors.toMap(
                                                result -> (Long) result[0],
                                                result -> ((RecruitmentStatus) result[1]).name()));
        }
}
