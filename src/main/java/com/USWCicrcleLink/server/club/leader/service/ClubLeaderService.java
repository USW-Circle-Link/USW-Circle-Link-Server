package com.USWCicrcleLink.server.club.leader.service;

import com.USWCicrcleLink.server.category.repository.ClubCategoryMappingRepository;
import com.USWCicrcleLink.server.category.repository.ClubCategoryRepository;
import com.USWCicrcleLink.server.club.application.domain.Aplict;
import com.USWCicrcleLink.server.club.application.domain.AplictStatus;
import com.USWCicrcleLink.server.club.application.dto.ApplicantResultsRequest;
import com.USWCicrcleLink.server.club.application.dto.ApplicantsResponse;
import com.USWCicrcleLink.server.club.application.dto.AplictDto;
import com.USWCicrcleLink.server.club.application.repository.AplictRepository;
import com.USWCicrcleLink.server.club.domain.*;
import com.USWCicrcleLink.server.club.repository.*;
import com.USWCicrcleLink.server.category.domain.ClubCategory;
import com.USWCicrcleLink.server.category.domain.ClubCategoryMapping;
import com.USWCicrcleLink.server.club.clubInfo.domain.ClubInfo;
import com.USWCicrcleLink.server.club.clubInfo.domain.ClubInfoPhoto;
import com.USWCicrcleLink.server.club.clubInfo.repository.ClubInfoPhotoRepository;
import com.USWCicrcleLink.server.club.clubInfo.repository.ClubInfoRepository;
import com.USWCicrcleLink.server.club.leader.domain.Leader;
import com.USWCicrcleLink.server.club.leader.dto.club.*;
import com.USWCicrcleLink.server.club.leader.dto.clubMembers.*;
import com.USWCicrcleLink.server.club.leader.repository.LeaderRepository;
import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.*;
import com.USWCicrcleLink.server.global.response.ApiResponse;
import com.USWCicrcleLink.server.global.s3File.Service.S3FileUploadService;
import com.USWCicrcleLink.server.global.s3File.dto.S3FileResponse;
import com.USWCicrcleLink.server.global.security.details.CustomLeaderDetails;
import com.USWCicrcleLink.server.user.profile.domain.MemberType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ClubLeaderService {
    private final ClubRepository clubRepository;
    private final ClubInfoRepository clubInfoRepository;
    private final ClubMembersRepository clubMembersRepository;
    private final AplictRepository aplictRepository;
    private final ClubInfoPhotoRepository clubInfoPhotoRepository;
    private final ClubMainPhotoRepository clubMainPhotoRepository;

    private final ClubHashtagRepository clubHashtagRepository;
    private final ClubCategoryRepository clubCategoryRepository;
    private final ClubCategoryMappingRepository clubCategoryMappingRepository;
    private final S3FileUploadService s3FileUploadService;
    private final FcmServiceImpl fcmService;
    private final LeaderRepository leaderRepository;

    private final com.USWCicrcleLink.server.global.security.jwt.JwtProvider jwtProvider;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    private final com.USWCicrcleLink.server.user.service.PasswordService passwordService;

    // 최대 사진 순서(업로드, 삭제)
    int PHOTO_LIMIT = 5;

    private final String S3_MAINPHOTO_DIR = "mainPhoto/";
    private final String S3_INFOPHOTO_DIR = "introPhoto/"; // Keep bucket path same but rename constant

    // 동아리 접근 권한 확인
    public Club validateLeaderAccess(UUID clubuuid) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomLeaderDetails leaderDetails = (CustomLeaderDetails) authentication.getPrincipal();
        if (!clubuuid.equals(leaderDetails.getClubuuid())) {
            throw new ClubLeaderException(ExceptionType.CLUB_LEADER_ACCESS_DENIED);
        }

        return clubRepository.findByClubuuid(clubuuid)
                .orElseThrow(() -> new ClubException(ExceptionType.CLUB_NOT_EXISTS));
    }

    // 약관 동의 여부 업데이트
    public ApiResponse<String> updateAgreedTermsTrue() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication.getPrincipal() instanceof CustomLeaderDetails leaderDetails)) {
            throw new UserException(ExceptionType.USER_NOT_EXISTS);
        }

        Leader leader = leaderDetails.leader();
        leader.setAgreeTerms(true);
        leaderRepository.save(leader);
        return new ApiResponse<>("약관 동의 완료");
    }

    public ApiResponse<String> updatePassword(com.USWCicrcleLink.server.club.leader.dto.LeaderUpdatePwRequest request,
            jakarta.servlet.http.HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication.getPrincipal() instanceof CustomLeaderDetails leaderDetails)) {
            throw new UserException(ExceptionType.USER_NOT_EXISTS);
        }

        Leader leader = leaderDetails.leader();

        if (!passwordEncoder.matches(request.getLeaderPw(), leader.getLeaderPw())) {
            throw new UserException(ExceptionType.USER_PASSWORD_NOT_MATCH);
        }

        if (passwordEncoder.matches(request.getNewPw(), leader.getLeaderPw())) {
            throw new UserException(ExceptionType.USER_PASSWORD_NOT_REUSE);
        }

        passwordService.validatePassword(request.getNewPw(), request.getConfirmNewPw());

        leader.updatePw(passwordEncoder.encode(request.getNewPw()));
        leaderRepository.save(leader);

        java.util.UUID leaderUUID = leader.getLeaderUUID();
        jwtProvider.deleteRefreshToken(leaderUUID);
        jwtProvider.deleteRefreshTokenCookie(response);

        return new ApiResponse<>("비밀번호가 변경되었습니다.");
    }

    // 동아리 기본 정보 조회 -> 동아리 프로필 조회로 변경
    @Transactional(readOnly = true)
    public ApiResponse<ClubProfileResponse> getClubProfile(UUID clubUUID) {

        Club club = validateLeaderAccess(clubUUID);

        // 동아리 메인 사진 조회
        Optional<ClubMainPhoto> clubMainPhoto = Optional
                .ofNullable(clubMainPhotoRepository.findByClub_ClubId(club.getClubId()));

        String mainPhotoUrl = clubMainPhoto.map(
                photo -> s3FileUploadService.generatePresignedGetUrl(photo.getClubMainPhotoS3Key()))
                .orElse(null);

        // 동아리 해시태그 조회
        List<String> clubHashtags = clubHashtagRepository.findByClubClubId(club.getClubId())
                .stream().map(ClubHashtag::getClubHashtag).collect(Collectors.toList());

        // 동아리 카테고리 조회
        List<String> clubCategories = clubCategoryMappingRepository.findByClubClubId(club.getClubId())
                .stream().map(mapping -> mapping.getClubCategory().getClubCategoryName()).collect(Collectors.toList());

        return new ApiResponse<>("동아리 기본 정보 조회 완료",
                new ClubProfileResponse(mainPhotoUrl, club, clubHashtags, clubCategories));
    }

    /**
     * 동아리 기본 정보 변경 -> 동아리 프로필 변경
     */
    public ApiResponse<UpdateClubProfileResponse> updateClubProfile(UUID clubUUID,
            ClubProfileRequest clubProfileRequest,
            MultipartFile mainPhoto) throws IOException {
        // 동아리 회장 유효성 검증
        Club club = validateLeaderAccess(clubUUID);

        // 사진 업데이트
        String mainPhotoUrl = updateClubMainPhoto(club.getClubId(), mainPhoto);

        // clubProfileRequest가 있을 때만 정보 업데이트
        if (clubProfileRequest != null) {
            // 동아리 회장 이름 변경 시 약관 동의 갱신 필요
            updateLeaderAgreementIfNameChanged(club, clubProfileRequest.getLeaderName());

            // 해시태그 업데이트
            updateClubHashtags(club, clubProfileRequest.getClubHashtag());

            // 카테고리 업데이트
            updateClubCategories(club, clubProfileRequest.getClubCategoryName());

            // 동아리 정보 업데이트
            club.updateClubInfo(clubProfileRequest.getLeaderName(), clubProfileRequest.getLeaderHp(),
                    clubProfileRequest.getClubInsta(), clubProfileRequest.getClubRoomNumber());

            log.info("동아리 기본 정보 변경 완료 - Club UUID: {}, Club Name: {}", club.getClubuuid(), club.getClubName());
        }

        return new ApiResponse<>("동아리 기본 정보 변경 완료", new UpdateClubProfileResponse(mainPhotoUrl));
    }

    // 동아리 회장 이름 변경 시 약관 동의 갱신
    private void updateLeaderAgreementIfNameChanged(Club club, String newLeaderName) {

        if (!Objects.equals(club.getLeaderName(), newLeaderName)) {
            Leader leader = leaderRepository.findByClubuuid(club.getClubuuid())
                    .orElseThrow(() -> new ClubLeaderException(ExceptionType.CLUB_LEADER_NOT_EXISTS));

            leader.setAgreeTerms(false);
            leaderRepository.save(leader);
            log.debug("회장 이름 변경으로 약관 동의 상태 초기화 - Leader ID: {}", leader.getLeaderId());
        }
    }

    // 동아리 해시태그 업데이트
    private void updateClubHashtags(Club club, List<String> newHashtags) {
        if (newHashtags == null || newHashtags.isEmpty())
            return;

        Set<String> newHashtagsSet = new HashSet<>(newHashtags);
        List<String> existingHashtags = clubHashtagRepository.findHashtagsByClubId(club.getClubId());

        clubHashtagRepository.deleteAllByClub_ClubIdAndClubHashtagNotIn(club.getClubId(), newHashtagsSet);

        // 새 해시태그 중 추가할 항목만 필터링하여 일괄 삽입
        List<ClubHashtag> newHashtagsToInsert = newHashtagsSet.stream()
                .filter(newHashtag -> !existingHashtags.contains(newHashtag))
                .map(newHashtag -> ClubHashtag.builder().club(club).clubHashtag(newHashtag).build())
                .toList();

        clubHashtagRepository.saveAll(newHashtagsToInsert);
    }

    // 동아리 카테고리 업데이트
    private void updateClubCategories(Club club, List<String> newCategories) {
        if (newCategories == null || newCategories.isEmpty())
            return;

        Set<String> newCategoriesSet = new HashSet<>(newCategories);
        List<ClubCategoryMapping> existingMappings = clubCategoryMappingRepository.findByClub_ClubId(club.getClubId());
        Set<String> existingCategoryNames = existingMappings.stream()
                .map(mapping -> mapping.getClubCategory().getClubCategoryName())
                .collect(Collectors.toSet());

        clubCategoryMappingRepository.deleteAllByClub_ClubIdAndClubCategory_ClubCategoryNameNotIn(club.getClubId(),
                newCategoriesSet);

        // 새 카테고리 중 추가할 항목만 필터링하여 일괄 삽입
        List<ClubCategoryMapping> newMappings = newCategoriesSet.stream()
                .filter(categoryName -> !existingCategoryNames.contains(categoryName))
                .map(categoryName -> {
                    ClubCategory clubCategory = clubCategoryRepository.findByClubCategoryName(categoryName)
                            .orElseThrow(() -> new ClubException(ExceptionType.CATEGORY_NOT_FOUND));
                    return ClubCategoryMapping.builder().club(club).clubCategory(clubCategory).build();
                })
                .toList();

        clubCategoryMappingRepository.saveAll(newMappings);
    }

    // 동아리 메인 사진 업데이트
    private String updateClubMainPhoto(Long clubId, MultipartFile mainPhoto) throws IOException {
        if (clubId == null) {
            throw new ClubPhotoException(ExceptionType.CLUB_ID_NOT_EXISTS);
        }

        if (mainPhoto == null || mainPhoto.isEmpty()) {
            return clubMainPhotoRepository.findS3KeyByClubId(clubId).orElse(null);
        }

        return processClubMainPhoto(clubId, mainPhoto);
    }

    // 기존 대표 사진 삭제 및 새로운 파일 업로드
    private String processClubMainPhoto(Long clubId, MultipartFile mainPhoto) throws IOException {
        clubMainPhotoRepository.findS3KeyByClubId(clubId).ifPresent(s3FileUploadService::deleteFile);

        return saveClubMainPhoto(mainPhoto, clubId);
    }

    // 사진 메타데이터 업데이트 및 S3 업로드
    private String saveClubMainPhoto(MultipartFile mainPhoto, Long clubId) throws IOException {
        if (clubId == null) {
            throw new ClubPhotoException(ExceptionType.CLUB_ID_NOT_EXISTS);
        }

        if (mainPhoto == null || mainPhoto.isEmpty()) {
            throw new ClubPhotoException(ExceptionType.CLUB_MAINPHOTO_NOT_EXISTS);
        }

        S3FileResponse s3FileResponse = s3FileUploadService.uploadFile(mainPhoto, S3_MAINPHOTO_DIR);

        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ClubException(ExceptionType.CLUB_NOT_EXISTS));

        Optional<ClubMainPhoto> existingPhoto = clubMainPhotoRepository.findByClub(club);

        existingPhoto.ifPresent(photo -> {
            clubMainPhotoRepository.delete(photo);
            clubMainPhotoRepository.flush();
        });

        ClubMainPhoto clubMainPhoto = ClubMainPhoto.builder()
                .club(club)
                .clubMainPhotoName(mainPhoto.getOriginalFilename())
                .clubMainPhotoS3Key(s3FileResponse.getS3FileName())
                .build();

        clubMainPhotoRepository.save(clubMainPhoto);

        return s3FileResponse.getPresignedUrl();
    }

    // 동아리 요약 조회
    @Transactional(readOnly = true)
    public ClubSummaryResponse getClubSummary(UUID clubUUID) {
        Club club = validateLeaderAccess(clubUUID);

        // 동아리 소개 조회 -> ClubInfo (was ClubIntro)
        ClubInfo clubInfo = clubInfoRepository.findByClubClubId(club.getClubId())
                .orElseThrow(() -> new ClubException(ExceptionType.CLUB_INFO_NOT_EXISTS));

        // clubHashtag 조회
        List<String> clubHashtags = clubHashtagRepository.findByClubClubId(club.getClubId())
                .stream().map(ClubHashtag::getClubHashtag).toList();

        // 동아리 카테고리 조회
        List<String> clubCategories = clubCategoryMappingRepository.findByClubClubId(club.getClubId())
                .stream().map(mapping -> mapping.getClubCategory().getClubCategoryName()).toList();

        // 동아리 메인 사진 조회
        ClubMainPhoto clubMainPhoto = clubMainPhotoRepository.findByClub(club).orElse(null);

        // 동아리 소개 사진 조회 -> ClubInfoPhoto (was ClubIntroPhoto)
        List<ClubInfoPhoto> clubInfoPhotos = clubInfoPhotoRepository.findByClubInfo(clubInfo);

        // S3에서 메인 사진 URL 생성 (기본 URL 또는 null 처리)
        String mainPhotoUrl = (clubMainPhoto != null)
                ? s3FileUploadService.generatePresignedGetUrl(clubMainPhoto.getClubMainPhotoS3Key())
                : null;

        // S3에서 소개 사진 URL 생성 (소개 사진이 없을 경우 빈 리스트)
        List<String> infoPhotoUrls = clubInfoPhotos.isEmpty() // intro -> info
                ? Collections.emptyList()
                : clubInfoPhotos.stream()
                        .sorted(Comparator.comparingInt(ClubInfoPhoto::getOrder)) // ClubIntroPhoto -> ClubInfoPhoto
                        .map(photo -> s3FileUploadService.generatePresignedGetUrl(photo.getClubInfoPhotoS3Key())) // getClubIntroPhotoS3Key
                                                                                                                  // ->
                                                                                                                  // getClubInfoPhotoS3Key
                        .collect(Collectors.toList());

        return new ClubSummaryResponse(club, clubHashtags, clubCategories, clubInfo, mainPhotoUrl, infoPhotoUrls);
    }

    // 동아리 소개 조회 -> getClubInfo (was getClubIntro)
    @Transactional(readOnly = true)
    public ApiResponse<LeaderClubInfoResponse> getClubInfo(UUID clubUUID) { // Renamed from getClubIntro
        Club club = validateLeaderAccess(clubUUID);

        // 동아리 소개 조회
        ClubInfo clubInfo = clubInfoRepository.findByClubClubId(club.getClubId())
                .orElseThrow(() -> new ClubException(ExceptionType.CLUB_INFO_NOT_EXISTS));

        // 동아리 소개 사진 조회
        List<ClubInfoPhoto> clubInfoPhotos = clubInfoPhotoRepository.findByClubInfo(clubInfo);

        // S3에서 소개 사진 URL 생성 (소개 사진이 없을 경우 빈 리스트)
        List<String> infoPhotoUrls = clubInfoPhotos.isEmpty()
                ? Collections.emptyList()
                : clubInfoPhotos.stream()
                        .sorted(Comparator.comparingInt(ClubInfoPhoto::getOrder))
                        .map(photo -> s3FileUploadService.generatePresignedGetUrl(photo.getClubInfoPhotoS3Key()))
                        .collect(Collectors.toList());

        return new ApiResponse<>("동아리 소개 조회 완료", new LeaderClubInfoResponse(club, clubInfo, infoPhotoUrls));
    }

    // 동아리 소개 변경 -> updateClubInfo (was updateClubIntro)
    public ApiResponse updateClubInfo(UUID clubUUID, ClubInfoRequest clubInfoRequest, // Renamed from updateClubIntro
                                                                                      // and ClubIntroRequest
            List<MultipartFile> infoPhotos) throws IOException { // introPhotos -> infoPhotos

        Club club = validateLeaderAccess(clubUUID);

        ClubInfo clubInfo = clubInfoRepository.findByClubClubId(club.getClubId())
                .orElseThrow(() -> new ClubException(ExceptionType.CLUB_INFO_NOT_EXISTS));

        // 모집 상태가 null일 때 예외 처리
        if (clubInfoRequest.getRecruitmentStatus() == null) {
            throw new ClubException(ExceptionType.INVALID_RECRUITMENT_STATUS);
        }

        // 삭제할 사진 확인
        if (clubInfoRequest.getDeletedOrders() != null && !clubInfoRequest.getDeletedOrders().isEmpty()) {
            // 순서 개수, 범위 검증
            validateOrderValues(clubInfoRequest.getDeletedOrders());

            for (int i = 0; i < clubInfoRequest.getDeletedOrders().size(); i++) {// 하나씩 삭제
                int deletingOrder = clubInfoRequest.getDeletedOrders().get(i);

                ClubInfoPhoto deletingPhoto = clubInfoPhotoRepository
                        .findByClubInfo_ClubInfoIdAndOrder(clubInfo.getClubInfoId(), deletingOrder) // getClubIntroId ->
                                                                                                    // getClubInfoId
                        .orElseThrow(() -> new ClubPhotoException(ExceptionType.PHOTO_ORDER_MISS_MATCH));

                s3FileUploadService.deleteFile(deletingPhoto.getClubInfoPhotoS3Key());

                deletingPhoto.updateClubInfoPhoto("", "", deletingOrder); // updateClubIntroPhoto -> updateClubInfoPhoto
                clubInfoPhotoRepository.save(deletingPhoto);

                log.debug("소개 사진 삭제 완료: {}", deletingPhoto.getOrder());
            }
        }

        // 각 사진의 조회 presignedUrls
        List<String> presignedUrls = new ArrayList<>();

        // 동아리 소개 사진을 넣을 경우
        if (infoPhotos != null && !infoPhotos.isEmpty() && clubInfoRequest.getOrders() != null
                && !clubInfoRequest.getOrders().isEmpty()) {

            // 순서 개수, 범위 검증
            validateOrderValues(clubInfoRequest.getOrders());

            if (infoPhotos.size() > PHOTO_LIMIT) {// 최대 5장 업로드
                throw new FileException(ExceptionType.MAXIMUM_FILE_LIMIT_EXCEEDED);
            }

            // N번째 사진 1장씩
            for (int i = 0; i < infoPhotos.size(); i++) {
                MultipartFile infoPhoto = infoPhotos.get(i); // introPhoto -> infoPhoto
                int order = clubInfoRequest.getOrders().get(i);

                // 동아리 소개 사진이 존재하지 않으면 순서 스킵
                if (infoPhoto == null || infoPhoto.isEmpty()) {
                    continue;
                }

                ClubInfoPhoto existingPhoto = clubInfoPhotoRepository
                        .findByClubInfo_ClubInfoIdAndOrder(clubInfo.getClubInfoId(), order) // getClubIntroId ->
                                                                                            // getClubInfoId
                        .orElseThrow(() -> new ClubPhotoException(ExceptionType.PHOTO_ORDER_MISS_MATCH));

                S3FileResponse s3FileResponse;

                // N번째 동아리 소개 사진 존재할 경우
                if (!Optional.ofNullable(existingPhoto.getClubInfoPhotoName()).orElse("").isEmpty() && // getClubIntroPhotoName
                        !Optional.ofNullable(existingPhoto.getClubInfoPhotoS3Key()).orElse("").isEmpty()) { // getClubIntroPhotoS3Key

                    // 기존 S3 파일 삭제
                    s3FileUploadService.deleteFile(existingPhoto.getClubInfoPhotoS3Key());
                    log.debug("기존 소개 사진 삭제 완료: {}", existingPhoto.getClubInfoPhotoS3Key());
                }
                // 새로운 파일 업로드 및 메타 데이터 업데이트
                s3FileResponse = updateClubInfoPhotoAndS3File(infoPhoto, existingPhoto, order);

                // 업로드된 사진의 사전 서명된 URL을 리스트에 추가
                presignedUrls.add(s3FileResponse.getPresignedUrl());
            }
        }

        // 소개 글, 모집 글, google form 저장
        clubInfo.updateClubInfo(clubInfoRequest.getClubInfo(), clubInfoRequest.getClubRecruitment(), // updateClubIntro
                                                                                                     // ->
                                                                                                     // updateClubInfo
                clubInfoRequest.getGoogleFormUrl());
        clubInfoRepository.save(clubInfo);

        log.debug("{} 동아리 소개 변경 완료", club.getClubName());
        return new ApiResponse<>("동아리 소개 변경 완료", new UpdateClubInfoResponse(presignedUrls));
    }

    private void validateOrderValues(List<Integer> orders) {
        // 순서 개수 체크
        if (orders.size() < 1 || orders.size() > PHOTO_LIMIT) {// 0 이하 6이상
            throw new ClubPhotoException(ExceptionType.PHOTO_ORDER_MISS_MATCH);
        }

        // 순서 값
        for (int order : orders) {
            if (order < 1 || order > PHOTO_LIMIT) { // 1 ~ 5 사이여야 함
                throw new ClubPhotoException(ExceptionType.PHOTO_ORDER_MISS_MATCH);
            }
        }

    }

    private S3FileResponse updateClubInfoPhotoAndS3File(MultipartFile infoPhoto, ClubInfoPhoto existingPhoto, // introPhoto
                                                                                                              // ->
                                                                                                              // infoPhoto
            int order) throws IOException {
        // 새로운 파일 업로드
        S3FileResponse s3FileResponse = s3FileUploadService.uploadFile(infoPhoto, S3_INFOPHOTO_DIR);

        // null 체크 후 값 설정
        String newPhotoName = infoPhoto.getOriginalFilename() != null ? infoPhoto.getOriginalFilename() : "";
        String newS3Key = s3FileResponse.getS3FileName() != null ? s3FileResponse.getS3FileName() : "";

        // s3key 및 photoname 업데이트
        existingPhoto.updateClubInfoPhoto(newPhotoName, newS3Key, order); // updateClubIntroPhoto -> updateClubInfoPhoto
        clubInfoPhotoRepository.save(existingPhoto);
        log.debug("사진 정보 저장 및 업데이트 완료: {}", s3FileResponse.getS3FileName());

        return s3FileResponse;
    }

    // 모집 상태 조회
    @Transactional(readOnly = true)
    public ApiResponse<RecruitmentStatusResponse> getRecruitmentStatus(UUID clubUUID) {
        Club club = validateLeaderAccess(clubUUID);

        ClubInfo clubInfo = clubInfoRepository.findByClubClubId(club.getClubId())
                .orElseThrow(() -> new ClubException(ExceptionType.CLUB_INFO_NOT_EXISTS));

        return new ApiResponse<>("모집 상태 조회 완료", new RecruitmentStatusResponse(clubInfo.getRecruitmentStatus()));
    }

    // 동아리 모집 상태 변경
    public ApiResponse toggleRecruitmentStatus(UUID clubUUID) {

        Club club = validateLeaderAccess(clubUUID);

        ClubInfo clubInfo = clubInfoRepository.findByClubClubId(club.getClubId())
                .orElseThrow(() -> new ClubException(ExceptionType.CLUB_INFO_NOT_EXISTS));
        log.debug("동아리 소개 조회 결과: {}", clubInfo);

        // 모집 상태 현재와 반전
        clubInfo.toggleRecruitmentStatus();
        clubRepository.save(club);

        return new ApiResponse<>("동아리 모집 상태 변경 완료", clubInfo.getRecruitmentStatus());
    }

    // 소속 동아리원 조회(구, 성능 비교용)
    // ... (rest of the file as is, no changes needed for members/application parts)

    // 소속 동아리 회원 조회(가나다순 정렬)
    @Transactional(readOnly = true)
    public ApiResponse<List<ClubMembersResponse>> getClubMembers(UUID clubUUID) {

        Club club = validateLeaderAccess(clubUUID);

        List<ClubMembers> findClubMembers = clubMembersRepository.findAllWithProfileByName(club.getClubId());

        // 동아리원과 프로필 조회
        List<ClubMembersResponse> memberProfiles = findClubMembers.stream()
                .map(cm -> new ClubMembersResponse(
                        cm.getClubMemberUUID(),
                        cm.getProfile()))
                .collect(toList());

        return new ApiResponse<>("소속 동아리 회원 가나다순 조회 완료", memberProfiles);
    }

    // ... (remaining methods)

    // 소속 동아리 회원 조회(정회원/ 비회원 정렬)
    @Transactional(readOnly = true)
    public ApiResponse<List<ClubMembersResponse>> getClubMembersByMemberType(UUID clubUUID, MemberType memberType) {

        Club club = validateLeaderAccess(clubUUID);

        List<ClubMembers> findClubMembers = clubMembersRepository.findAllWithProfileByMemberType(club.getClubId(),
                memberType);

        // 동아리원과 프로필 조회
        List<ClubMembersResponse> memberProfiles = findClubMembers.stream()
                .map(cm -> new ClubMembersResponse(
                        cm.getClubMemberUUID(),
                        cm.getProfile()))
                .collect(toList());

        return new ApiResponse<>("소속 동아리 회원 조회 완료", memberProfiles);
    }

    // 소속 동아리원 삭제
    public ApiResponse deleteClubMembers(UUID clubUUID, List<ClubMembersDeleteRequest> clubMemberUUIDList) {

        Club club = validateLeaderAccess(clubUUID);

        List<UUID> clubMemberUUIDs = clubMemberUUIDList.stream()
                .map(ClubMembersDeleteRequest::getClubMemberUUID)
                .toList();

        // 동아리 회원인지 확인
        List<ClubMembers> membersToDelete = clubMembersRepository.findByClubClubIdAndClubMemberUUIDIn(club.getClubId(),
                clubMemberUUIDs);

        // 조회된 수와 요청한 수와 같은지(다르면 다른 동아리 회원이 존재)
        if (membersToDelete.size() != clubMemberUUIDList.size()) {
            throw new ClubMemberException(ExceptionType.CLUB_MEMBER_NOT_EXISTS);
        }

        // 동아리 회원 삭제
        clubMembersRepository.deleteAll(membersToDelete);

        return new ApiResponse<>("동아리 회원 삭제 완료", clubMemberUUIDList);
    }

    // 동아리 지원자 조회 (전체 또는 상태별)
    @Transactional(readOnly = true)
    public ApiResponse<List<ApplicantsResponse>> getApplicants(UUID clubUUID, AplictStatus status) {
        Club club = validateLeaderAccess(clubUUID);

        List<Aplict> aplicts;
        if (status == null) {
            // 상태 조건 없이 전체 조회
            aplicts = aplictRepository.findAllWithProfileByClubId(club.getClubId());
        } else {
            // 특정 상태 조회
            aplicts = aplictRepository.findAllWithProfileByClubIdAndStatus(club.getClubId(), status);
        }

        List<ApplicantsResponse> applicants = aplicts.stream()
                .map(ap -> new ApplicantsResponse(
                        ap.getAplictUUID(),
                        ap.getProfile()))
                .toList();

        return new ApiResponse<>("동아리 지원자 조회 완료", applicants);
    }

    // 최종 합격자 알림 (개별/일괄 처리 가능)
    public void updateApplicantResults(UUID clubUUID, List<ApplicantResultsRequest> results) throws IOException {
        Club club = validateLeaderAccess(clubUUID);

        for (ApplicantResultsRequest result : results) {

            Aplict applicant = aplictRepository.findByAplictUUID(result.getAplictUUID())
                    .orElseThrow(() -> new AplictException(ExceptionType.APPLICANT_NOT_EXISTS));

            // 해당 동아리 지원자가 맞는지 검증
            if (!applicant.getClub().getClubuuid().equals(clubUUID)) {
                throw new AplictException(ExceptionType.APPLICANT_NOT_EXISTS);
            }

            AplictStatus currentStatus = applicant.getAplictStatus();

            // WAIT 상태이면 알림 보내지 않음
            if (currentStatus == AplictStatus.WAIT) {
                continue;
            }

            if (currentStatus == AplictStatus.PASS) {
                // 이미 멤버인지 확인 (중복 가입 방지)
                boolean isMember = clubMembersRepository.findByProfileProfileIdAndClubClubId(
                        applicant.getProfile().getProfileId(), club.getClubId()).isPresent();

                if (!isMember) {
                    ClubMembers newClubMembers = ClubMembers.builder()
                            .club(club)
                            .profile(applicant.getProfile())
                            .build();
                    clubMembersRepository.save(newClubMembers);
                }

                // 삭제 (지원자 DB에서 제거)
                aplictRepository.delete(applicant);
                log.debug("합격 처리/알림 및 지원서 삭제: {}", applicant.getAplictUUID());
                fcmService.sendMessageTo(applicant, currentStatus);

            } else if (currentStatus == AplictStatus.FAIL) {
                // 삭제 (지원자 DB에서 제거)
                aplictRepository.delete(applicant);
                log.debug("불합격 알림 및 지원서 삭제: {}", applicant.getAplictUUID());
                fcmService.sendMessageTo(applicant, currentStatus);
            }

            // aplictRepository.save(applicant); // 삭제했으므로 저장 불필요
        }
    }

    // 지원서 상세 조회 (Leader)
    @Transactional(readOnly = true)
    public AplictDto.DetailResponse getApplicationDetail(UUID clubUUID, UUID aplictUUID) {
        validateLeaderAccess(clubUUID);

        Aplict aplict = aplictRepository.findByAplictUUID(aplictUUID)
                .orElseThrow(() -> new AplictException(ExceptionType.APPLICANT_NOT_EXISTS));

        // Ensure the application belongs to this club
        if (!aplict.getClub().getClubuuid().equals(clubUUID)) {
            throw new AplictException(ExceptionType.APLICT_NOT_EXISTS);
        }

        List<AplictDto.QnAResponse> qnaList = aplict.getAnswers().stream()
                .map(a -> new AplictDto.QnAResponse(a.getFormQuestion().getContent(), a.getAnswerText()))
                .toList();

        return new AplictDto.DetailResponse(
                aplict.getAplictUUID(),
                aplict.getProfile().getUserName(),
                aplict.getProfile().getStudentNumber(),
                aplict.getProfile().getMajor(),
                aplict.getSubmittedAt(),
                aplict.getAplictStatus(),
                qnaList);
    }

    // 지원자 상태 변경 (Leader)
    public void updateAplictStatus(UUID clubUUID, UUID aplictUUID, AplictStatus status) {
        validateLeaderAccess(clubUUID);

        Aplict aplict = aplictRepository.findByAplictUUID(aplictUUID)
                .orElseThrow(() -> new AplictException(ExceptionType.APPLICANT_NOT_EXISTS));

        if (!aplict.getClub().getClubuuid().equals(clubUUID)) {
            throw new AplictException(ExceptionType.APLICT_NOT_EXISTS);
        }

        // 상태 업데이트
        aplict.updateFailedAplictStatus(status);

        aplictRepository.save(aplict);
        log.debug("지원자 상태 변경 완료: {} -> {}", aplictUUID, status);
    }
}
