package com.USWCicrcleLink.server.admin.admin.service;

import com.USWCicrcleLink.server.admin.admin.domain.Admin;
import com.USWCicrcleLink.server.admin.admin.dto.ClubCreationRequest;
import com.USWCicrcleLink.server.admin.admin.dto.ClubCreationResponse;
import com.USWCicrcleLink.server.admin.admin.dto.ClubAdminListResponse;
import com.USWCicrcleLink.server.club.club.domain.Club;
import com.USWCicrcleLink.server.club.club.domain.ClubMainPhoto;
import com.USWCicrcleLink.server.club.club.domain.RecruitmentStatus;
import com.USWCicrcleLink.server.club.club.repository.ClubMainPhotoRepository;
import com.USWCicrcleLink.server.club.club.repository.ClubRepository;
import com.USWCicrcleLink.server.club.clubIntro.domain.ClubIntro;
import com.USWCicrcleLink.server.club.clubIntro.domain.ClubIntroPhoto;
import com.USWCicrcleLink.server.club.clubIntro.repository.ClubIntroPhotoRepository;
import com.USWCicrcleLink.server.club.clubIntro.repository.ClubIntroRepository;
import com.USWCicrcleLink.server.clubLeader.domain.Leader;
import com.USWCicrcleLink.server.clubLeader.repository.LeaderRepository;
import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.AdminException;
import com.USWCicrcleLink.server.global.exception.errortype.ClubException;
import com.USWCicrcleLink.server.global.security.domain.Role;
import com.USWCicrcleLink.server.global.security.util.CustomAdminDetails;
import com.USWCicrcleLink.server.global.util.validator.InputValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final LeaderRepository leaderRepository;
    private final ClubRepository clubRepository;
    private final ClubIntroRepository clubIntroRepository;
    private final ClubMainPhotoRepository clubMainPhotoRepository;
    private final ClubIntroPhotoRepository clubIntroPhotoRepository;
    private final PasswordEncoder passwordEncoder;

    // 동아리 목록 조회(웹)
    public List<ClubAdminListResponse> getAllClubs() {
        List<ClubAdminListResponse> results;
        try {
            results = clubRepository.findAllWithMemberAndLeaderCount();
        } catch (Exception e) {
            throw new ClubException(ExceptionType.ClUB_CHECKING_ERROR);
        }
        return results;
    }

    // 동아리 생성(웹)
    public ClubCreationResponse createClub(ClubCreationRequest request) {

        // SecurityContextHolder에서 인증 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomAdminDetails adminDetails = (CustomAdminDetails) authentication.getPrincipal();
        Admin admin = adminDetails.admin();

        // 관리자 비밀번호 검증
        if (!passwordEncoder.matches(request.getAdminPw(), admin.getAdminPw())) {
            throw new AdminException(ExceptionType.ADMIN_PASSWORD_NOT_MATCH);
        }

        // 동아리 회장 비밀번호 확인
        if (!request.getLeaderPw().equals(request.getLeaderPwConfirm())) {
            throw new ClubException(ExceptionType.ClUB_LEADER_PASSWORD_NOT_MATCH);
        }

        log.debug("동아리 회장 비밀번호 확인 성공");

        // Leader 계정 중복 확인
        if (leaderRepository.existsByLeaderAccount(request.getLeaderAccount())) {
            throw new ClubException(ExceptionType.LEADER_ACCOUNT_ALREADY_EXISTS);
        }

        // Club 이름 중복 확인
        if (clubRepository.existsByClubName(request.getClubName())) {
            throw new ClubException(ExceptionType.CLUB_NAME_ALREADY_EXISTS);
        }

        // 입력값 검증 (XSS 공격 방지)
        String sanitizedClubName = InputValidator.sanitizeContent(request.getClubName());
        String sanitizedLeaderAccount = InputValidator.sanitizeContent(request.getLeaderAccount());

        // Club 생성 및 저장
        Club club = Club.builder()
                .clubName(sanitizedClubName)
                .department(request.getDepartment())
                .leaderName("")
                .leaderHp("")
                .clubInsta("")
                .build();
        clubRepository.save(club);
        log.debug("동아리 생성 성공: {}", club.getClubName());

        // Leader 생성 및 저장
        Leader leader = Leader.builder()
                .leaderAccount(sanitizedLeaderAccount)
                .leaderPw(passwordEncoder.encode(request.getLeaderPw()))  // 비밀번호는 암호화해서 저장
                .leaderUUID(UUID.randomUUID())
                .role(Role.LEADER)
                .club(club)
                .build();
        leaderRepository.save(leader);
        log.debug("동아리 회장 생성 성공: {}", leader.getLeaderAccount());

        // ClubMainPhoto 생성 및 저장
        ClubMainPhoto mainPhoto = ClubMainPhoto.builder()
                .club(club)
                .clubMainPhotoName("")
                .clubMainPhotoS3Key("")
                .build();
        clubMainPhotoRepository.save(mainPhoto);
        log.debug("동아리 메인 사진 생성 성공");

        // ClubIntro 생성 및 저장
        ClubIntro clubIntro = ClubIntro.builder()
                .club(club)
                .clubIntro("")
                .googleFormUrl("")
                .recruitmentStatus(RecruitmentStatus.CLOSE)
                .build();
        clubIntroRepository.save(clubIntro);
        log.debug("동아리 소개 생성 성공: {}", clubIntro.getClubIntro());

        // ClubIntroPhoto 기본값 설정 (5개 생성)
        List<ClubIntroPhoto> introPhotos = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            ClubIntroPhoto introPhoto = ClubIntroPhoto.builder()
                    .clubIntro(clubIntro)
                    .clubIntroPhotoName("")
                    .clubIntroPhotoS3Key("")
                    .order(i)  // 순서 설정 (1~5)
                    .build();
            introPhotos.add(introPhoto);
        }
        clubIntroPhotoRepository.saveAll(introPhotos);
        log.debug("동아리 소개 사진 5개 생성 성공");

        return new ClubCreationResponse(club, leader);
    }

    // 동아리 삭제(웹)
    public void deleteClub(Long clubId, String adminPw) {

        // SecurityContextHolder에서 인증 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomAdminDetails adminDetails = (CustomAdminDetails) authentication.getPrincipal();
        Admin admin = adminDetails.admin();

        // 관리자 비밀번호 검증
        if (!passwordEncoder.matches(adminPw, admin.getAdminPw())) {
            throw new AdminException(ExceptionType.ADMIN_PASSWORD_NOT_MATCH);
        }

        // 동아리 존재 여부 확인
        clubRepository.findById(clubId)
                .orElseThrow(() -> new ClubException(ExceptionType.CLUB_NOT_EXISTS));

        // 동아리 및 관련 종속 엔티티와 S3 파일 삭제
        clubRepository.deleteClubAndDependencies(clubId);
        log.debug("동아리 삭제 성공: clubId = {}", clubId);
    }
}