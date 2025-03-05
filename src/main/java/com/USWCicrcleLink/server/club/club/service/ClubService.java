package com.USWCicrcleLink.server.club.club.service;

import com.USWCicrcleLink.server.admin.admin.dto.AdminClubIntroResponse;
import com.USWCicrcleLink.server.admin.admin.mapper.ClubCategoryMapper;
import com.USWCicrcleLink.server.club.club.domain.*;
import com.USWCicrcleLink.server.club.club.dto.ClubCategoryResponse;
import com.USWCicrcleLink.server.club.club.dto.ClubInfoListResponse;
import com.USWCicrcleLink.server.club.club.dto.ClubListByClubCategoryResponse;
import com.USWCicrcleLink.server.club.club.dto.ClubListResponse;
import com.USWCicrcleLink.server.club.club.repository.*;
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

    // 기존 회원가입시 동아리 리스트 출력
    @Transactional(readOnly = true)
    public List<ClubInfoListResponse> getAllClubsInfo() {
        log.debug("전체 동아리 리스트 조회");
        List<Club> clubs = clubRepository.findAll();

        return clubs.stream()
                .map(club -> {
                    // ClubMainPhoto 조회
                    ClubMainPhoto clubMainPhoto = clubMainPhotoRepository.findByClub(club).orElse(null);

                    // S3 presigned URL 생성 (기본 URL 또는 null 처리)
                    String mainPhotoUrl = (clubMainPhoto != null)
                            ? s3FileUploadService.generatePresignedGetUrl(clubMainPhoto.getClubMainPhotoS3Key())
                            : null;

                    // DTO 생성
                    return new ClubInfoListResponse(club, mainPhotoUrl);  // 전체 동아리 조회용 DTO로 수정
                })
                .collect(Collectors.toList());
    }

    /**
     * 전체 동아리 리스트 조회 (OPEN API)
     */
    public List<ClubListResponse> getAllClubs() {
        List<Club> clubs = clubRepository.findAll();
        return mapToClubListResponse(clubs);
    }

    /**
     * 모집 중 동아리 리스트 조회 (OPEN API)
     */
    public List<ClubListResponse> getOpenClubs() {
        List<Long> openClubIds = clubIntroRepository.findOpenClubIds();
        List<Club> clubs = clubRepository.findByClubIds(openClubIds);
        return mapToClubListResponse(clubs);
    }

    private List<ClubListResponse> mapToClubListResponse(List<Club> clubs) {
        if (clubs.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> clubIds = clubs.stream()
                .map(Club::getClubId)
                .collect(Collectors.toList());

        Map<Long, String> mainPhotoUrls = getClubMainPhotoUrls(clubIds);
        Map<Long, List<String>> clubHashtags = getClubHashtags(clubIds);

        return clubs.stream()
                .map(club -> new ClubListResponse(
                        club.getClubUUID(),
                        club.getClubName(),
                        mainPhotoUrls.getOrDefault(club.getClubId(), null),
                        club.getDepartment().name(),
                        clubHashtags.getOrDefault(club.getClubId(), Collections.emptyList())
                ))
                .collect(Collectors.toList());
    }

    // 특정 동아리 리스트의 메인 사진 URL 조회
    private Map<Long, String> getClubMainPhotoUrls(List<Long> clubIds) {
        return clubMainPhotoRepository.findByClubIds(clubIds).stream()
                .collect(Collectors.toMap(
                        photo -> photo.getClub().getClubId(),
                        photo -> s3FileUploadService.generatePresignedGetUrl(photo.getClubMainPhotoS3Key())
                ));
    }

    // 특정 동아리 리스트의 해시태그 조회
    private Map<Long, List<String>> getClubHashtags(List<Long> clubIds) {
        return clubHashtagRepository.findByClubIds(clubIds).stream()
                .collect(Collectors.groupingBy(
                        tag -> tag.getClub().getClubId(),
                        Collectors.mapping(ClubHashtag::getClubHashtag, Collectors.toList())
                ));
    }

    /**
     * 카테고리 필터 적용한 전체 동아리 리스트 조회 (OPEN API)
     */
    public List<ClubListByClubCategoryResponse> getAllClubsByClubCategories(List<UUID> clubCategoryUUIDs) {
        return fetchClubsByCategories(clubCategoryUUIDs, false);
    }

    /**
     * 카테고리 필터 적용한 모집 중 동아리 리스트 조회 (OPEN API)
     */
    public List<ClubListByClubCategoryResponse> getOpenClubsByClubCategories(List<UUID> clubCategoryUUIDs) {
        return fetchClubsByCategories(clubCategoryUUIDs, true);
    }

    // 카테고리 필터가 적용된 동아리 조회
    private List<ClubListByClubCategoryResponse> fetchClubsByCategories(List<UUID> clubCategoryUUIDs, boolean isOpenFilter) {
        if (clubCategoryUUIDs == null || clubCategoryUUIDs.size() > 3) {
            throw new BaseException(ExceptionType.INVALID_CATEGORY_COUNT);
        }

        List<Long> clubCategoryIds = clubCategoryRepository.findClubCategoryIdsByUUIDs(clubCategoryUUIDs);
        if (clubCategoryIds.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> openClubIds = isOpenFilter ? new HashSet<>(clubIntroRepository.findOpenClubIds()) : null;

        List<ClubCategoryMapping> categoryMappings = clubCategoryMappingRepository.findByClubCategoryIds(clubCategoryIds)
                .stream()
                .filter(mapping -> openClubIds == null || openClubIds.contains(mapping.getClub().getClubId()))
                .toList();

        Set<Long> clubIds = categoryMappings.stream()
                .map(mapping -> mapping.getClub().getClubId())
                .collect(Collectors.toSet());

        if (clubIds.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, String> mainPhotoUrls = getClubMainPhotoUrls(new ArrayList<>(clubIds));
        Map<Long, List<String>> clubHashtags = getClubHashtags(new ArrayList<>(clubIds));

        Map<Long, List<Club>> clubsByCategory = categoryMappings.stream()
                .collect(Collectors.groupingBy(
                        mapping -> mapping.getClubCategory().getClubCategoryId(),
                        Collectors.mapping(ClubCategoryMapping::getClub, Collectors.toList())
                ));

        return clubCategoryIds.stream()
                .map(categoryId -> {
                    ClubCategory category = clubCategoryRepository.findById(categoryId)
                            .orElseThrow(() -> new BaseException(ExceptionType.CATEGORY_NOT_FOUND));

                    List<ClubListResponse> clubResponses = clubsByCategory.getOrDefault(categoryId, Collections.emptyList())
                            .stream()
                            .map(club -> new ClubListResponse(
                                    club.getClubUUID(),
                                    club.getClubName(),
                                    mainPhotoUrls.getOrDefault(club.getClubId(), null),
                                    club.getDepartment().name(),
                                    clubHashtags.getOrDefault(club.getClubId(), Collections.emptyList())
                            ))
                            .collect(Collectors.toList());

                    return new ClubListByClubCategoryResponse(
                            category.getClubCategoryUUID(),
                            category.getClubCategoryName(),
                            clubResponses
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * 전체 카테고리 리스트 조회
     */
    public List<ClubCategoryResponse> getAllClubCategories() {
        return clubCategoryRepository.findAll().stream()
                .map(ClubCategoryMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 동아리 소개/모집글 페이지 조회 (ADMIN, USER)
     */
    public AdminClubIntroResponse getClubIntro(UUID clubUUID) {
        Club club = clubRepository.findByClubUUID(clubUUID)
                .orElseThrow(() -> new ClubException(ExceptionType.CLUB_NOT_EXISTS));

        Long clubId = club.getClubId();

        ClubIntro clubIntro = clubIntroRepository.findByClubClubId(clubId)
                .orElseThrow(() -> new ClubException(ExceptionType.CLUB_INTRO_NOT_EXISTS));

        String mainPhotoUrl = clubMainPhotoRepository.findByClubClubId(clubId)
                .map(photo -> s3FileUploadService.generatePresignedGetUrl(photo.getClubMainPhotoS3Key()))
                .orElse(null);

        List<String> introPhotoUrls = getIntroPhotoUrls(clubId);

        List<String> hashtags = getClubHashtags(clubId);

        List<String> clubCategoryNames = getClubCategoryNames(clubId);

        return new AdminClubIntroResponse(
                club.getClubUUID(),
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
                clubIntro.getClubRecruitment()
        );
    }

    // 특정 동아리의 소개 사진 URL 리스트 가져오기
    private List<String> getIntroPhotoUrls(Long clubId) {
        return clubIntroPhotoRepository.findByClubIntroClubId(clubId).stream()
                .sorted(Comparator.comparingInt(ClubIntroPhoto::getOrder))
                .map(photo -> s3FileUploadService.generatePresignedGetUrl(photo.getClubIntroPhotoS3Key()))
                .collect(Collectors.toList());
    }

    // 특정 동아리의 해시태그 리스트 가져오기
    private List<String> getClubHashtags(Long clubId) {
        return clubHashtagRepository.findByClubClubId(clubId).stream()
                .map(ClubHashtag::getClubHashtag)
                .collect(Collectors.toList());
    }

    // 특정 동아리의 카테고리 이름 리스트 가져오기
    private List<String> getClubCategoryNames(Long clubId) {
        return clubCategoryMappingRepository.findByClubClubId(clubId).stream()
                .map(mapping -> mapping.getClubCategory().getClubCategoryName())
                .collect(Collectors.toList());
    }
}