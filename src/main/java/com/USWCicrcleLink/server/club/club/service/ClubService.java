package com.USWCicrcleLink.server.club.club.service;

import com.USWCicrcleLink.server.admin.admin.mapper.ClubCategoryMapper;
import com.USWCicrcleLink.server.club.club.domain.*;
import com.USWCicrcleLink.server.club.club.dto.ClubCategoryResponse;
import com.USWCicrcleLink.server.club.club.dto.ClubInfoListResponse;
import com.USWCicrcleLink.server.club.club.dto.ClubListByClubCategoryResponse;
import com.USWCicrcleLink.server.club.club.dto.ClubListResponse;
import com.USWCicrcleLink.server.club.club.repository.*;
import com.USWCicrcleLink.server.club.clubIntro.domain.ClubIntro;
import com.USWCicrcleLink.server.club.clubIntro.domain.ClubIntroPhoto;
import com.USWCicrcleLink.server.admin.admin.dto.AdminClubIntroResponse;
import com.USWCicrcleLink.server.club.clubIntro.repository.ClubIntroPhotoRepository;
import com.USWCicrcleLink.server.club.clubIntro.repository.ClubIntroRepository;
import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.BaseException;
import com.USWCicrcleLink.server.global.exception.errortype.ClubException;
import com.USWCicrcleLink.server.global.exception.errortype.ClubIntroException;
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

    // 전체 동아리 리스트 조회 (모바일)
    @Transactional(readOnly = true)
    public List<ClubListResponse> getAllClubs() {

        List<Club> clubs = clubRepository.findAll();

        List<Long> clubIds = clubs.stream()
                .map(Club::getClubId)
                .toList();

        Map<Long, String> mainPhotoUrls = clubMainPhotoRepository.findByClubIds(clubIds)
                .stream()
                .collect(Collectors.toMap(
                        photo -> photo.getClub().getClubId(),
                        photo -> s3FileUploadService.generatePresignedGetUrl(photo.getClubMainPhotoS3Key())
                ));

        Map<Long, List<String>> clubHashtags = clubHashtagRepository.findByClubIds(clubIds)
                .stream()
                .collect(Collectors.groupingBy(
                        tag -> tag.getClub().getClubId(),
                        Collectors.mapping(ClubHashtag::getClubHashtag, Collectors.toList())
                ));

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
                    return new ClubInfoListResponse(club,mainPhotoUrl);  // 전체 동아리 조회용 DTO로 수정
                })
                .collect(Collectors.toList());
    }

    // 관심 카테고리 필터 적용한 전체 동아리 리스트 조회 (모바일)
    @Transactional(readOnly = true)
    public List<ClubListByClubCategoryResponse> getAllClubsByClubCategories(List<UUID> clubCategoryUUIDs) {
        validateCategoryLimit(clubCategoryUUIDs);

        List<Long> clubCategoryIds = clubCategoryRepository.findClubCategoryIdsByUUIDs(clubCategoryUUIDs);
        if (clubCategoryIds.isEmpty()) {
            throw new BaseException(ExceptionType.CATEGORY_NOT_FOUND);
        }

        List<Club> clubs = clubCategoryMappingRepository.findClubsByCategoryIds(clubCategoryIds);

        List<Long> clubIds = clubs.stream().map(Club::getClubId).toList();

        Map<Long, String> mainPhotoUrls = clubMainPhotoRepository.findByClubIds(clubIds)
                .stream()
                .collect(Collectors.toMap(
                        photo -> photo.getClub().getClubId(),
                        photo -> s3FileUploadService.generatePresignedGetUrl(photo.getClubMainPhotoS3Key())
                ));

        Map<Long, List<String>> clubHashtags = clubHashtagRepository.findByClubIds(clubIds)
                .stream()
                .collect(Collectors.groupingBy(
                        tag -> tag.getClub().getClubId(),
                        Collectors.mapping(ClubHashtag::getClubHashtag, Collectors.toList())
                ));

        return clubCategoryIds.stream()
                .map(categoryId -> {
                    List<ClubListResponse> clubResponses = clubs.stream()
                            .map(club -> new ClubListResponse(
                                    club.getClubUUID(),
                                    club.getClubName(),
                                    mainPhotoUrls.getOrDefault(club.getClubId(), null),
                                    club.getDepartment().name(),
                                    clubHashtags.getOrDefault(club.getClubId(), Collections.emptyList())
                            ))
                            .collect(Collectors.toList());

                    ClubCategory category = clubCategoryRepository.findById(categoryId)
                            .orElseThrow(() -> new BaseException(ExceptionType.CATEGORY_NOT_FOUND));

                    return new ClubListByClubCategoryResponse(
                            category.getClubCategoryUUID(),
                            category.getClubCategoryName(),
                            clubResponses
                    );
                })
                .collect(Collectors.toList());
    }

    // 모집 중 동아리 리스트 조회 (모바일)
    @Transactional(readOnly = true)
    public List<ClubListResponse> getOpenClubs() {

        List<Long> openClubIds = clubIntroRepository.findOpenClubIds();

        List<Club> clubs = clubRepository.findByClubIds(openClubIds);

        Map<Long, String> mainPhotoUrls = clubMainPhotoRepository.findByClubIds(openClubIds)
                .stream()
                .collect(Collectors.toMap(
                        photo -> photo.getClub().getClubId(),
                        photo -> s3FileUploadService.generatePresignedGetUrl(photo.getClubMainPhotoS3Key())
                ));

        Map<Long, List<String>> clubHashtags = clubHashtagRepository.findByClubIds(openClubIds)
                .stream()
                .collect(Collectors.groupingBy(
                        tag -> tag.getClub().getClubId(),
                        Collectors.mapping(ClubHashtag::getClubHashtag, Collectors.toList())
                ));

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

    // 관심 카테고리 필터 적용한 모집 중 동아리 리스트 조회 (모바일)
    @Transactional(readOnly = true)
    public List<ClubListByClubCategoryResponse> getOpenClubsByClubCategories(List<UUID> clubCategoryUUIDs) {
        validateCategoryLimit(clubCategoryUUIDs);

        List<Long> clubCategoryIds = clubCategoryRepository.findClubCategoryIdsByUUIDs(clubCategoryUUIDs);
        if (clubCategoryIds.isEmpty()) {
            throw new BaseException(ExceptionType.CATEGORY_NOT_FOUND);
        }

        List<Long> openClubIds = clubIntroRepository.findOpenClubIds();

        List<Club> clubs = clubCategoryMappingRepository.findOpenClubsByCategoryIds(clubCategoryIds, openClubIds);

        List<Long> clubIds = clubs.stream().map(Club::getClubId).toList();

        Map<Long, String> mainPhotoUrls = clubMainPhotoRepository.findByClubIds(clubIds)
                .stream()
                .collect(Collectors.toMap(
                        photo -> photo.getClub().getClubId(),
                        photo -> s3FileUploadService.generatePresignedGetUrl(photo.getClubMainPhotoS3Key())
                ));

        Map<Long, List<String>> clubHashtags = clubHashtagRepository.findByClubIds(clubIds)
                .stream()
                .collect(Collectors.groupingBy(
                        tag -> tag.getClub().getClubId(),
                        Collectors.mapping(ClubHashtag::getClubHashtag, Collectors.toList())
                ));

        return clubCategoryIds.stream()
                .map(categoryId -> {
                    List<ClubListResponse> clubResponses = clubs.stream()
                            .map(club -> new ClubListResponse(
                                    club.getClubUUID(),
                                    club.getClubName(),
                                    mainPhotoUrls.getOrDefault(club.getClubId(), null),
                                    club.getDepartment().name(),
                                    clubHashtags.getOrDefault(club.getClubId(), Collections.emptyList())
                            ))
                            .collect(Collectors.toList());

                    ClubCategory category = clubCategoryRepository.findById(categoryId)
                            .orElseThrow(() -> new BaseException(ExceptionType.CATEGORY_NOT_FOUND));

                    return new ClubListByClubCategoryResponse(
                            category.getClubCategoryUUID(),
                            category.getClubCategoryName(),
                            clubResponses
                    );
                })
                .collect(Collectors.toList());
    }

    // 카테고리 개수 검증 (최대 3개)
    private void validateCategoryLimit(List<UUID> clubCategoryUUIDs) {
        if (Optional.ofNullable(clubCategoryUUIDs).orElse(Collections.emptyList()).size() > 3) {
            throw new BaseException(ExceptionType.INVALID_CATEGORY_COUNT);
        }
    }

    // 카테고리 조회
    @Transactional(readOnly = true)
    public List<ClubCategoryResponse> getAllClubCategories() {
        List<ClubCategory> clubCategories = clubCategoryRepository.findAll();
        return ClubCategoryMapper.toDtoList(clubCategories);
    }


    // 동아리 소개/모집글 페이지 조회 (웹 - 운영팀, 모바일)
    @Transactional(readOnly = true)
    public AdminClubIntroResponse getClubIntro(UUID clubUUID) {
        Club club = clubRepository.findByClubUUID(clubUUID)
                .orElseThrow(() -> new ClubException(ExceptionType.CLUB_NOT_EXISTS));

        Long clubId = club.getClubId();

        ClubIntro clubIntro = clubIntroRepository.findByClubClubId(clubId)
                .orElseThrow(() -> new ClubIntroException(ExceptionType.CLUB_INTRO_NOT_EXISTS));

        String mainPhotoUrl = clubMainPhotoRepository.findByClubClubId(clubId)
                .map(photo -> s3FileUploadService.generatePresignedGetUrl(photo.getClubMainPhotoS3Key()))
                .orElse(null);

        List<String> introPhotoUrls = clubIntroPhotoRepository.findByClubIntroClubId(clubId)
                .stream()
                .sorted(Comparator.comparingInt(ClubIntroPhoto::getOrder))
                .map(photo -> s3FileUploadService.generatePresignedGetUrl(photo.getClubIntroPhotoS3Key()))
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
                club.getClubUUID(),
                mainPhotoUrl,
                introPhotoUrls,
                club.getClubName(),
                club.getLeaderName(),
                club.getLeaderHp(),
                club.getClubInsta(),
                clubIntro.getClubIntro(),
                clubIntro.getRecruitmentStatus(),
                hashtags,
                clubCategoryNames,
                club.getClubRoomNumber(),
                clubIntro.getClubRecruitment()
        );
    }
}