package com.USWCicrcleLink.server.admin.service;

import com.USWCicrcleLink.server.admin.domain.Admin;
import com.USWCicrcleLink.server.admin.dto.AdminClubCreationRequest;
import com.USWCicrcleLink.server.admin.dto.AdminClubListResponse;
import com.USWCicrcleLink.server.admin.dto.AdminClubPageListResponse;
import com.USWCicrcleLink.server.admin.dto.AdminPwRequest;
import com.USWCicrcleLink.server.club.domain.Club;
import com.USWCicrcleLink.server.club.domain.ClubMainPhoto;
import com.USWCicrcleLink.server.club.domain.RecruitmentStatus;
import com.USWCicrcleLink.server.club.repository.ClubMainPhotoRepository;
import com.USWCicrcleLink.server.club.repository.ClubRepository;
import com.USWCicrcleLink.server.club.clubInfo.domain.ClubInfo;
import com.USWCicrcleLink.server.club.clubInfo.domain.ClubInfoPhoto;
import com.USWCicrcleLink.server.club.clubInfo.repository.ClubInfoPhotoRepository;
import com.USWCicrcleLink.server.club.clubInfo.repository.ClubInfoRepository;
import com.USWCicrcleLink.server.club.leader.domain.Leader;
import com.USWCicrcleLink.server.club.leader.repository.LeaderRepository;
import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.AdminException;
import com.USWCicrcleLink.server.global.exception.errortype.BaseException;
import com.USWCicrcleLink.server.global.exception.errortype.ClubException;
import com.USWCicrcleLink.server.global.security.details.CustomAdminDetails;
import com.USWCicrcleLink.server.global.security.jwt.domain.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
public class AdminClubService {

    private final LeaderRepository leaderRepository;
    private final ClubRepository clubRepository;
    private final ClubInfoRepository clubInfoRepository;
    private final ClubMainPhotoRepository clubMainPhotoRepository;
    private final ClubInfoPhotoRepository clubInfoPhotoRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 메인 페이지(ADMIN) - 동아리 목록 조회
     */
    @Transactional(readOnly = true)
    public AdminClubPageListResponse getAllClubs(Pageable pageable) {
        Page<AdminClubListResponse> clubs = clubRepository.findAllWithMemberAndLeaderCount(pageable);
        log.debug("동아리 목록 조회 성공 - 총 {}개", clubs.getTotalElements());

        return AdminClubPageListResponse.builder()
                .content(clubs.getContent())
                .totalPages(clubs.getTotalPages())
                .totalElements(clubs.getTotalElements())
                .currentPage(clubs.getNumber())
                .build();
    }

    /**
     * 동아리 생성(ADMIN) - 동아리 생성 완료하기
     */
    public void createClub(AdminClubCreationRequest request) {
        Admin admin = getAuthenticatedAdmin();

        if (!request.getLeaderPw().equals(request.getLeaderPwConfirm())) {
            throw new ClubException(ExceptionType.ClUB_LEADER_PASSWORD_NOT_MATCH);
        }

        validateLeaderAccount(request.getLeaderAccount());
        validateClubName(request.getClubName());

        if (!passwordEncoder.matches(request.getAdminPw(), admin.getAdminPw())) {
            throw new AdminException(ExceptionType.ADMIN_PASSWORD_NOT_MATCH);
        }

        Club club = createClubEntity(request);
        log.info("동아리 생성 성공 - Name: {}", club.getClubName());

        createLeaderAccount(request.getLeaderAccount(), request.getLeaderPw(), club);
        createClubDefaultData(club);
    }

    // 동아리 생성 - 동아리 생성
    private Club createClubEntity(AdminClubCreationRequest request) {
        Club club = Club.builder()
                .clubName(request.getClubName())
                .department(request.getDepartment())
                .leaderName("")
                .leaderHp("")
                .clubInsta("")
                .clubRoomNumber(request.getClubRoomNumber())
                .build();

        return clubRepository.save(club);
    }

    // 동아리 생성 - 회장 계정 생성
    private void createLeaderAccount(String leaderAccount, String leaderPw, Club club) {
        Leader leader = Leader.builder()
                .leaderAccount(leaderAccount)
                .leaderPw(passwordEncoder.encode(leaderPw))
                .leaderUUID(UUID.randomUUID())
                .role(Role.LEADER)
                .club(club)
                .build();
        leaderRepository.save(leader);
        log.info("회장 계정 생성 성공 - uuid: {}", leader.getLeaderUUID());
    }

    // 동아리 생성 - 기본 동아리 데이터 생성
    private void createClubDefaultData(Club club) {
        createClubMainPhoto(club);
        ClubInfo clubInfo = createClubInfo(club);
        createClubInfoPhotos(clubInfo);
    }

    private void createClubMainPhoto(Club club) {
        clubMainPhotoRepository.save(
                ClubMainPhoto.builder()
                        .club(club)
                        .clubMainPhotoName("")
                        .clubMainPhotoS3Key("")
                        .build());
    }

    private ClubInfo createClubInfo(Club club) {
        return clubInfoRepository.save(
                ClubInfo.builder()
                        .club(club)
                        .clubInfo("")
                        .clubRecruitment("")
                        .googleFormUrl("")
                        .recruitmentStatus(RecruitmentStatus.CLOSE)
                        .build());
    }

    private void createClubInfoPhotos(ClubInfo clubInfo) {
        List<ClubInfoPhoto> infoPhotos = new ArrayList<>();

        for (int i = 1; i <= 5; i++) {
            infoPhotos.add(ClubInfoPhoto.builder()
                    .clubInfo(clubInfo)
                    .clubInfoPhotoName("")
                    .clubInfoPhotoS3Key("")
                    .order(i)
                    .build());
        }

        clubInfoPhotoRepository.saveAll(infoPhotos);
        log.debug("기본 동아리 소개 사진 5개 생성 완료 - Club ID: {}", clubInfo.getClub().getClubId());
    }

    // 동아리 생성 - 동아리 회장 아이디 중복 확인
    public void validateLeaderAccount(String leaderAccount) {
        if (leaderRepository.existsByLeaderAccount(leaderAccount)) {
            log.warn("동아리 회장 계정 중복 - LeaderAccount: {}", leaderAccount);
            throw new ClubException(ExceptionType.LEADER_ACCOUNT_ALREADY_EXISTS);
        }
    }

    // 동아리 생성 - 동아리 이름 중복 확인
    public void validateClubName(String clubName) {
        if (clubRepository.existsByClubName(clubName)) {
            log.warn("동아리명 중복 - ClubName: {}", clubName);
            throw new ClubException(ExceptionType.CLUB_NAME_ALREADY_EXISTS);
        }
    }

    /**
     * 동아리 삭제(ADMIN) - 동아리 삭제 완료하기
     */
    @Transactional
    public void deleteClub(UUID clubUUID, AdminPwRequest request) {
        Admin admin = getAuthenticatedAdmin();

        if (!passwordEncoder.matches(request.getAdminPw(), admin.getAdminPw())) {
            throw new AdminException(ExceptionType.ADMIN_PASSWORD_NOT_MATCH);
        }

        Long clubId = clubRepository.findClubIdByClubuuid(clubUUID)
                .orElseThrow(() -> {
                    log.warn("동아리 삭제 실패 - 존재하지 않는 Club UUID: {}", clubUUID);
                    return new ClubException(ExceptionType.CLUB_NOT_EXISTS);
                });

        try {
            clubRepository.deleteClubAndDependencies(clubId);
            log.info("동아리 삭제 성공 - Club uuid: {}", clubUUID);
        } catch (Exception e) {
            log.error("동아리 삭제 중 오류 발생 - Club uuid: {}, 오류: {}", clubUUID, e.getMessage());
            throw new BaseException(ExceptionType.SERVER_ERROR, e);
        }
    }

    /**
     * 인증된 ADMIN 정보 가져오기
     */
    private Admin getAuthenticatedAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomAdminDetails adminDetails = (CustomAdminDetails) authentication.getPrincipal();
        return adminDetails.admin();
    }
}
