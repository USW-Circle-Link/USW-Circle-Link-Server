package com.USWCicrcleLink.server.clubLeader.service;

import com.USWCicrcleLink.server.aplict.domain.Aplict;
import com.USWCicrcleLink.server.aplict.domain.AplictStatus;
import com.USWCicrcleLink.server.aplict.dto.ApplicantResultsRequest;
import com.USWCicrcleLink.server.aplict.dto.ApplicantsResponse;
import com.USWCicrcleLink.server.aplict.repository.AplictRepository;
import com.USWCicrcleLink.server.club.club.domain.Club;
import com.USWCicrcleLink.server.club.club.domain.ClubMainPhoto;
import com.USWCicrcleLink.server.club.club.domain.ClubMembers;
import com.USWCicrcleLink.server.club.club.repository.ClubMainPhotoRepository;
import com.USWCicrcleLink.server.club.club.repository.ClubMembersRepository;
import com.USWCicrcleLink.server.club.club.repository.ClubRepository;
import com.USWCicrcleLink.server.club.clubIntro.domain.ClubIntro;
import com.USWCicrcleLink.server.club.clubIntro.domain.ClubIntroPhoto;
import com.USWCicrcleLink.server.club.clubIntro.dto.ClubIntroResponse;
import com.USWCicrcleLink.server.club.clubIntro.repository.ClubIntroPhotoRepository;
import com.USWCicrcleLink.server.club.clubIntro.repository.ClubIntroRepository;
import com.USWCicrcleLink.server.club.clubIntro.service.ClubIntroService;
import com.USWCicrcleLink.server.clubLeader.domain.Leader;
import com.USWCicrcleLink.server.clubLeader.dto.*;
import com.USWCicrcleLink.server.clubLeader.repository.LeaderRepository;
import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.*;
import com.USWCicrcleLink.server.global.response.ApiResponse;
import com.USWCicrcleLink.server.global.response.PageResponse;
import com.USWCicrcleLink.server.global.security.util.CustomLeaderDetails;
import com.USWCicrcleLink.server.global.util.s3File.Service.S3FileUploadService;
import com.USWCicrcleLink.server.global.util.s3File.dto.S3FileResponse;
import com.USWCicrcleLink.server.profile.domain.Profile;
import com.USWCicrcleLink.server.profile.repository.ProfileRepository;
import com.USWCicrcleLink.server.user.domain.User;
import com.USWCicrcleLink.server.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ClubLeaderService {
    private final ClubRepository clubRepository;
    private final ClubIntroRepository clubIntroRepository;
    private final ClubMembersRepository clubMembersRepository;
    private final AplictRepository aplictRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final ClubIntroPhotoRepository clubIntroPhotoRepository;
    private final ClubMainPhotoRepository clubMainPhotoRepository;
    private final ClubIntroService clubIntroService;

    private final S3FileUploadService s3FileUploadService;
    private final FcmServiceImpl fcmService;

    // 업로드 가능한 파일 갯수
    int FILE_LIMIT = 5;

    private final String S3_MAINPHOTO_DIR = "mainPhoto/";
    private final String S3_INTROPHOTO_DIR = "introPhoto/";

    // 동아리 기본 정보 조회
    @Transactional(readOnly = true)
    public ApiResponse<ClubInfoResponse> getClubInfo(Long clubId) {

        Club club = validateLeader(clubId);

        Optional<ClubMainPhoto> clubMainPhoto = Optional.ofNullable(clubMainPhotoRepository.findByClub_ClubId(club.getClubId()));

        // 사진이 있으면 url 없으면 null
        String mainPhotoUrl = clubMainPhoto.map(
                        photo -> s3FileUploadService.generatePresignedGetUrl(photo.getClubMainPhotoS3Key()))
                .orElse(null);

        ClubInfoResponse clubInfoResponse = new ClubInfoResponse(
                mainPhotoUrl,
                club.getClubName(),
                club.getLeaderName(),
                club.getLeaderHp(),
                club.getClubInsta(),
                club.getDepartment()
        );

        return new ApiResponse<>("동아리 기본 정보 조회 완료", clubInfoResponse);
    }

    // 동아리 기본 정보 변경
    public ApiResponse<UpdateClubInfoResponse> updateClubInfo(Long clubId, ClubInfoRequest clubInfoRequest, MultipartFile mainPhoto) throws IOException {

        Club club = validateLeader(clubId);

        // 기존 동아리 대표 사진 조회
        ClubMainPhoto existingPhoto = clubMainPhotoRepository.findByClub_ClubId(clubId);

        S3FileResponse s3FileResponse;

        // 기존 사진이 존재할 경우
        if (!existingPhoto.getClubMainPhotoS3Key().isEmpty() && !existingPhoto.getClubMainPhotoName().isEmpty()) {
            // 기존 S3 파일 삭제
            s3FileUploadService.deleteFile(existingPhoto.getClubMainPhotoS3Key());
            log.debug("기존 사진 삭제 완료: {}", existingPhoto.getClubMainPhotoS3Key());
        }

        // 새로운 파일 업로드 및 메타 데이터 업데이트
        s3FileResponse = updateClubMainPhotoAndS3File(mainPhoto, existingPhoto);

        // 동아리 기본 정보 변경
        club.updateClubInfo(clubInfoRequest.getLeaderName(), clubInfoRequest.getLeaderHp(), clubInfoRequest.getClubInsta());
        clubRepository.save(club);
        log.debug("동아리 기본 정보 변경 완료: {}", club.getClubName());

        return new ApiResponse<>("동아리 기본 정보 변경 완료", new UpdateClubInfoResponse(s3FileResponse.getPresignedUrl()));
    }

    private S3FileResponse updateClubMainPhotoAndS3File(MultipartFile mainPhoto, ClubMainPhoto existingPhoto) throws IOException {
        // 새로운 파일 업로드
        S3FileResponse s3FileResponse = s3FileUploadService.uploadFile(mainPhoto, S3_MAINPHOTO_DIR);

        // s3key 및 photoname 업데이트
        existingPhoto.updateClubMainPhoto(mainPhoto.getOriginalFilename(), s3FileResponse.getS3FileName());
        clubMainPhotoRepository.save(existingPhoto);
        log.debug("사진 정보 저장 및 업데이트 완료: {}", s3FileResponse.getS3FileName());

        return s3FileResponse;
    }

    // 동아리 상세 페이지 조회(웹, 모바일)
    @Transactional(readOnly = true)
    public ClubIntroResponse getClubIntro(Long clubId) {
        Club club = validateLeader(clubId);
        return clubIntroService.getClubIntroDetails(club);
    }

    // 동아리 소개 변경
    public ApiResponse updateClubIntro(Long clubId, ClubIntroRequest clubIntroRequest, List<MultipartFile> introPhotos) throws IOException {

        Club club = validateLeader(clubId);

        ClubIntro clubIntro = clubIntroRepository.findByClubClubId(club.getClubId())
                .orElseThrow(() -> new ClubIntroException(ExceptionType.CLUB_INTRO_NOT_EXISTS));

        // 각 사진의 presignedUrls
        List<String> presignedUrls = new ArrayList<>();

        // 동아리 소개 사진을 넣을 경우
        if (introPhotos != null && !introPhotos.isEmpty() && clubIntroRequest.getOrders() != null && !clubIntroRequest.getOrders().isEmpty()) {

            if (introPhotos.size() > FILE_LIMIT) {// 최대 5장 업로드
                throw new FileException(ExceptionType.MAXIMUM_FILE_LIMIT_EXCEEDED);
            }

            // N번째 사진 1장씩
            for (int i = 0; i < introPhotos.size(); i++) {
                MultipartFile introPhoto = introPhotos.get(i);
                int order = clubIntroRequest.getOrders().get(i);

                // 동아리 소개 사진이 존재하지 않으면 해당 순서는 건너뜁니다
                if (introPhoto == null || introPhoto.isEmpty()) {
                    continue;
                }

                ClubIntroPhoto existingPhoto = clubIntroPhotoRepository
                        .findByClubIntro_ClubIntroIdAndOrder(clubIntro.getClubIntroId(), order)
                        .orElseThrow(() -> new ClubPhotoException(ExceptionType.PHOTO_ORDER_MISS_MATCH));

                S3FileResponse s3FileResponse;

                // N번째 동아리 소개 사진 존재할 경우
                if (!existingPhoto.getClubIntroPhotoS3Key().isEmpty() && !existingPhoto.getClubIntroPhotoS3Key().isEmpty()) {
                    // 기존 S3 파일 삭제
                    s3FileUploadService.deleteFile(existingPhoto.getClubIntroPhotoS3Key());
                    log.debug("기존 사진 삭제 완료: {}", existingPhoto.getClubIntroPhotoS3Key());
                }
                // 새로운 파일 업로드 및 메타 데이터 업데이트
                s3FileResponse = updateClubIntroPhotoAndS3File(introPhoto, existingPhoto, order);

                // 업로드된 사진의 사전 서명된 URL을 리스트에 추가
                presignedUrls.add(s3FileResponse.getPresignedUrl());
            }
        }

        // 소개 글, google form 저장
        clubIntro.updateClubIntro(clubIntroRequest.getClubIntro(), clubIntroRequest.getGoogleFormUrl());
        clubIntroRepository.save(clubIntro);

        log.debug("{} 소개 저장 완료", club.getClubName());
        return new ApiResponse<>("동아리 소개 변경 완료", new UpdateClubIntroResponse(presignedUrls));
    }

    private S3FileResponse updateClubIntroPhotoAndS3File(MultipartFile introPhoto, ClubIntroPhoto existingPhoto, int order) throws IOException {
        // 새로운 파일 업로드
        S3FileResponse s3FileResponse = s3FileUploadService.uploadFile(introPhoto, S3_INTROPHOTO_DIR);

        // s3key 및 photoname 업데이트
        existingPhoto.updateClubIntroPhoto(introPhoto.getOriginalFilename(), s3FileResponse.getS3FileName(),order);
        clubIntroPhotoRepository.save(existingPhoto);
        log.debug("사진 정보 저장 및 업데이트 완료: {}", s3FileResponse.getS3FileName());

        return s3FileResponse;
    }

    // 동아리 모집 상태 변경
    public ApiResponse toggleRecruitmentStatus(Long clubId) {

        Club club = validateLeader(clubId);

        ClubIntro clubIntro = clubIntroRepository.findByClubClubId(club.getClubId())
                .orElseThrow(() -> new ClubIntroException(ExceptionType.CLUB_INTRO_NOT_EXISTS));
        log.debug("동아리 소개 조회 결과: {}", clubIntro);

        // 모집 상태 현재와 반전
        clubIntro.toggleRecruitmentStatus();
        clubRepository.save(club);

        return new ApiResponse<>("동아리 모집 상태 변경 완료", clubIntro.getRecruitmentStatus());
    }

    // 소속 동아리원 조회(구, 성능 비교용)
//    @Transactional(readOnly = true)
//    public ApiResponse<List<ClubMembersResponse>> findClubMembers(LeaderToken token) {
//
//        Club club = validateLeader(token);
//
//        // 해당 동아리원 조회(성능 비교)
////        List<ClubMembers> findClubMembers = clubMembersRepository.findByClub(club); // 일반
//        List<ClubMembers> findClubMembers = clubMembersRepository.findAllWithProfile(club.getClubId()); // 성능
//
//        // 동아리원과 프로필 조회
//        List<ClubMembersResponse> memberProfiles = findClubMembers.stream()
//                .map(cm -> new ClubMembersResponse(
//                        cm.getClubMemberId(),
//                        cm.getProfile()
//                ))
//                .collect(toList());
//
//        return new ApiResponse<>("소속 동아리원 조회 완료", memberProfiles);
//    }

    // 소속 동아리원 조회
    @Transactional(readOnly = true)
    public ApiResponse<PageResponse<ClubMembersResponse>> getClubMembers(Long clubId, int page, int size) {

        Club club = validateLeader(clubId);

        PageRequest pageable = PageRequest.of(page, size);

        Page<ClubMembers> findClubMembers = clubMembersRepository.findAllWithProfileByClubId(club.getClubId(), pageable);

        // 동아리원과 프로필 조회
        List<ClubMembersResponse> memberProfiles = findClubMembers.stream()
                .map(cm -> new ClubMembersResponse(
                        cm.getClubMemberId(),
                        cm.getProfile()
                ))
                .collect(toList());

        PageResponse<ClubMembersResponse> pageResponse = new PageResponse<>(
                memberProfiles,
                findClubMembers.getNumber(),
                findClubMembers.getSize(),
                findClubMembers.getTotalElements(),
                findClubMembers.getTotalPages()
        );

        return new ApiResponse<>("소속 동아리원 조회 완료", pageResponse);
    }

    // 소속 동아리원 삭제
    public ApiResponse deleteClubMember(Long clubMemberId, Long clubId) {

        Club club = validateLeader(clubId);

        // 동아리원 삭제
        clubMembersRepository.deleteById(clubMemberId);
        return new ApiResponse<>("동아리원 삭제 완료");
    }

    // 소속 동아리원 엑셀 다운
    @Transactional(readOnly = true)
    public void downloadExcel(Long clubId, HttpServletResponse response) {

        Club club = validateLeader(clubId);

        // 해당 동아리원 조회
        List<ClubMembers> findClubMembers = clubMembersRepository.findAllWithProfile(club.getClubId());

        // 동아리원의 프로필 조회 후 동아리원 정보로 정리
        List<ClubMembersExcelResponse> memberProfiles = findClubMembers.stream()
                .map(cm -> new ClubMembersExcelResponse(
                        cm.getProfile()
                ))
                .collect(toList());

        // 파일 이름 설정
        String fileName = club.getClubName() + "_회원_명단.xlsx";
        String encodedFileName;
        try {
            encodedFileName = URLEncoder.encode(fileName, "UTF-8");
        } catch (IOException e) {
            throw new FileException(ExceptionType.FILE_ENCODING_FAILED);
        }

        // Content-Disposition 헤더 설정
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition",
                "attachment; filename=" + encodedFileName);

        // 엑셀 파일 생성
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ) {
            // 시트 이름 설정
            Sheet sheet = workbook.createSheet(club.getClubName());

            // 표 바탕색
            CellStyle blueCellStyle = workbook.createCellStyle();
            applyCellStyle(blueCellStyle, new Color(74, 119, 202));

            // 표 시작 위치
            Row headerRow = sheet.createRow(0);

            // 카테고리 설정
            String[] columnHeaders = {"학과", "학번", "이름", "전화번호"};
            for (int i = 0; i < columnHeaders.length; i++) {// 셀 생성, 카테고리 부여
                Cell headerCell = headerRow.createCell(i);
                headerCell.setCellValue(columnHeaders[i]);
                headerCell.setCellStyle(blueCellStyle);
            }

            // DB값 엑셀 파일에 넣기
            int rowNum = 1;
            for (ClubMembersExcelResponse member : memberProfiles) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(member.getMajor());
                row.createCell(1).setCellValue(member.getStudentNumber());
                row.createCell(2).setCellValue(member.getUserName());
                row.createCell(3).setCellValue(member.getUserHp());
            }

            workbook.write(outputStream);
            outputStream.writeTo(response.getOutputStream());
            response.flushBuffer();
            log.debug("{} 파일 추출 완료", club.getClubName());
        } catch (IOException e) {
            throw new FileException(ExceptionType.FILE_CREATE_FAILED);
        }
    }

    // 엑셀 표 스타일 설정
    private void applyCellStyle(CellStyle cellStyle, Color color) {
        XSSFCellStyle xssfCellStyle = (XSSFCellStyle) cellStyle;
        xssfCellStyle.setFillForegroundColor(new XSSFColor(color, new DefaultIndexedColorMap()));
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // 글 정렬
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        // 표 그리기
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setBorderBottom(BorderStyle.THIN);
    }

    // 동아리 지원자 조회
    @Transactional(readOnly = true)
    public ApiResponse<PageResponse> getApplicants(Long clubId, int page, int size) {
        Club club = validateLeader(clubId);

        Pageable pageable = PageRequest.of(page, size);

        // 합/불 처리되지 않은 동아리 지원자 조회
        Page<Aplict> aplicts = aplictRepository.findAllWithProfileByClubId(
                club.getClubId(),
                pageable,
                false);
        List<ApplicantsResponse> applicants = aplicts.stream()
                .map(ap -> new ApplicantsResponse(
                        ap.getId(),
                        ap.getProfile()
                ))
                .collect(toList());

        PageResponse<ApplicantsResponse> pageResponse = new PageResponse<>(
                applicants,
                aplicts.getNumber(),
                aplicts.getSize(),
                aplicts.getTotalElements(),
                aplicts.getTotalPages()
        );

        return new ApiResponse<>("지원자 조회 완료", pageResponse);
    }

    // 최초 합격자 알림
    public void updateApplicantResults(Long clubId, List<ApplicantResultsRequest> results) throws IOException {
        Club club = validateLeader(clubId);

        // 동아리 지원자 전원 조회(최초 합격)
        List<Aplict> applicants = aplictRepository.findByClub_ClubIdAndChecked(club.getClubId(), false);

        // 선택된 지원자 수와 전체 동아리 지원자 수 비교
        validateTotalApplicants(applicants, results);

        // 지원자 검증(지원한 동아리 + 지원서 + check안된 상태)
        for (ApplicantResultsRequest result : results) {
            Aplict applicant = aplictRepository.findByClub_ClubIdAndIdAndChecked(
                            club.getClubId(),
                            result.getAplictId(),
                            false)
                    .orElseThrow(() -> new AplictException(ExceptionType.APPLICANT_NOT_EXISTS));

            // 합격 불합격 상태 업데이트
            // 합/불, checked, 삭제 날짜

            AplictStatus aplictResult = result.getAplictStatus();// 지원 결과 PASS/ FAIL
            if (aplictResult == AplictStatus.PASS) {
                applicant.updateAplictStatus(aplictResult, true, LocalDateTime.now().plusDays(4));
                fcmService.sendMessageTo(applicant, aplictResult);
                log.debug("합격 처리 완료: {}", applicant.getId());
            } else if (aplictResult == AplictStatus.FAIL) {
                applicant.updateAplictStatus(aplictResult, true, LocalDateTime.now().plusDays(4));
//                fcmService.sendMessageTo(applicant, aplictResult);
                log.debug("불합격 처리 완료: {}", applicant.getId());
            }

            aplictRepository.save(applicant);
        }
    }

    // 선택된 지원자 수와 전체 지원자 수 비교
    private void validateTotalApplicants(List<Aplict> applicants, List<ApplicantResultsRequest> results) {
        Set<Long> applicantIds = applicants.stream()
                .map(Aplict::getId)
                .collect(Collectors.toSet());

        Set<Long> requestedApplicantIds = results.stream()
                .map(ApplicantResultsRequest::getAplictId)
                .collect(Collectors.toSet());

        if (!requestedApplicantIds.equals(applicantIds)) {
            throw new AplictException(ExceptionType.APPLICANT_COUNT_MISMATCH);
        }
    }

    // 불합격자 조회
    @Transactional(readOnly = true)
    public ApiResponse<PageResponse> getFailedApplicants(Long clubId, int page, int size) {
        Club club = validateLeader(clubId);

        Pageable pageable = PageRequest.of(page, size);

        // 불합격자 동아리 지원자 조회
        Page<Aplict> aplicts = aplictRepository.findAllWithProfileByClubIdAndFailed(
                club.getClubId(),
                pageable,
                true,
                AplictStatus.FAIL);

        List<ApplicantsResponse> applicants = aplicts.stream()
                .map(ap -> new ApplicantsResponse(
                        ap.getId(),
                        ap.getProfile()
                ))
                .collect(toList());

        PageResponse<ApplicantsResponse> pageResponse = new PageResponse<>(
                applicants,
                aplicts.getNumber(),
                aplicts.getSize(),
                aplicts.getTotalElements(),
                aplicts.getTotalPages()
        );

        return new ApiResponse<>("불합격자 조회 완료", pageResponse);
    }

    // 동아리 지원자 추가 합격 처리
    public void updateFailedApplicantResults(Long clubId, List<ApplicantResultsRequest> results) throws IOException {
        Club club = validateLeader(clubId);

        // 지원자 검증(지원한 동아리 + 지원서 + check된 상태 + 불합)
        for (ApplicantResultsRequest result : results) {
            Aplict applicant = aplictRepository.findByClub_ClubIdAndIdAndCheckedAndAplictStatus(
                            club.getClubId(),
                            result.getAplictId(),
                            true,
                            AplictStatus.FAIL
                    )
                    .orElseThrow(() -> new AplictException(ExceptionType.ADDITIONAL_APPLICANT_NOT_EXISTS));

            // 합격 불합격 상태 업데이트
            // 합격
            AplictStatus aplictResult = result.getAplictStatus();
            applicant.updateFailedAplictStatus(aplictResult);
            fcmService.sendMessageTo(applicant, aplictResult);
            log.debug("합격 처리 완료: {}", applicant.getId());

            aplictRepository.save(applicant);
        }
    }

    public void updateFcmToken(FcmTokenTestRequest fcmTokenTestRequest) {
        User user = userRepository.findByUserAccount(fcmTokenTestRequest.getUserAccount())
                .orElseThrow(() -> new RuntimeException("유효한 회원이 없습니다."));

        Profile profile = profileRepository.findById(user.getUserId())
                .orElseThrow(() -> new RuntimeException("유효한 회원이 없습니다."));

        profile.updateFcmToken(fcmTokenTestRequest.getFcmToken());
        profileRepository.save(profile);
        log.debug("fcmToken 업데이트: {}", user.getUserAccount());
    }

    // 회장 검증 및 소속 동아리
    private Club validateLeader(Long clubId) {
        // SecurityContextHolder에서 인증 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomLeaderDetails leaderDetails = (CustomLeaderDetails) authentication.getPrincipal();
        Leader leader = leaderDetails.leader();
        log.debug("인증된 동아리 회장: {}", leader.getLeaderAccount());

        // 동아리 조회
        Club club = clubRepository.findById(leader.getClub().getClubId())
                .orElseThrow(() -> new ClubException(ExceptionType.CLUB_NOT_EXISTS));
        log.debug("동아리 조회 결과: {}", club.getClubName());

        // 요청된 clubId와 인증된 회장의 clubId 비교
        if (!club.getClubId().equals(clubId)) {
            throw new ClubLeaderException(ExceptionType.CLUB_LEADER_ACCESS_DENIED);
        }

        return club;
    }
}