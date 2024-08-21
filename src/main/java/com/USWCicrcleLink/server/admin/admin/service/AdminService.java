package com.USWCicrcleLink.server.admin.admin.service;

import com.USWCicrcleLink.server.admin.admin.domain.Admin;
import com.USWCicrcleLink.server.admin.admin.dto.ClubCreationRequest;
import com.USWCicrcleLink.server.admin.admin.dto.ClubCreationResponse;
import com.USWCicrcleLink.server.admin.admin.dto.ClubListResponse;
import com.USWCicrcleLink.server.admin.notice.domain.NoticePhoto;
import com.USWCicrcleLink.server.club.club.domain.Club;
import com.USWCicrcleLink.server.club.club.domain.ClubMainPhoto;
import com.USWCicrcleLink.server.club.club.domain.RecruitmentStatus;
import com.USWCicrcleLink.server.club.club.repository.ClubMainPhotoRepository;
import com.USWCicrcleLink.server.club.club.repository.ClubRepository;
import com.USWCicrcleLink.server.club.clubIntro.domain.ClubIntro;
import com.USWCicrcleLink.server.club.clubIntro.domain.ClubIntroPhoto;
import com.USWCicrcleLink.server.club.clubIntro.dto.ClubIntroResponse;
import com.USWCicrcleLink.server.club.clubIntro.repository.ClubIntroPhotoRepository;
import com.USWCicrcleLink.server.club.clubIntro.repository.ClubIntroRepository;
import com.USWCicrcleLink.server.clubLeader.domain.Leader;
import com.USWCicrcleLink.server.clubLeader.repository.LeaderRepository;
import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.AdminException;
import com.USWCicrcleLink.server.global.exception.errortype.ClubException;
import com.USWCicrcleLink.server.global.exception.errortype.ClubIntroException;
import com.USWCicrcleLink.server.global.security.domain.Role;
import com.USWCicrcleLink.server.global.security.util.CustomAdminDetails;
import com.USWCicrcleLink.server.global.util.s3File.Service.S3FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final LeaderRepository leaderRepository;
    private final ClubRepository clubRepository;
    private final ClubIntroRepository clubIntroRepository;

    // 동아리 목록 조회(웹)
    public List<ClubListResponse> getAllClubs() {
        List<ClubListResponse> results;
        try {
            results = clubRepository.findAllWithMemberAndLeaderCount();
        } catch (Exception e) {
            throw new ClubException(ExceptionType.ClUB_CHECKING_ERROR);
        }
        return results;
    }

    // 동아리 생성(웹)
    public ClubCreationResponse createClub(ClubCreationRequest clubRequest) {
        log.debug("동아리 생성 요청 시작");

        // SecurityContextHolder에서 인증 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomAdminDetails adminDetails = (CustomAdminDetails) authentication.getPrincipal();
        Admin admin = adminDetails.admin();

        if (admin.getAdminPw().equals(clubRequest.getAdminPw())) {
            log.debug("관리자 비밀번호 확인 성공");

            if (!clubRequest.getLeaderPw().equals(clubRequest.getLeaderPwConfirm())) {
                throw new RuntimeException("동아리 회장 비밀번호가 일치하지 않습니다.");
            }

            log.debug("동아리 회장 비밀번호 확인 성공");

            Leader leader = Leader.builder()
                    .leaderAccount(clubRequest.getLeaderAccount())
                    .leaderPw(clubRequest.getLeaderPw())
                    .role(Role.LEADER)
                    .build();
            leaderRepository.save(leader);
            log.debug("동아리 회장 생성 성공: {}", leader.getLeaderAccount());

            Club club = Club.builder()
                    .clubName(clubRequest.getClubName())
                    .department(clubRequest.getDepartment())
                    .leaderName(clubRequest.getLeaderAccount())
                    .build();
            clubRepository.save(club);
            log.debug("동아리 생성 성공: {}", club.getClubName());

            ClubIntro clubIntro = ClubIntro.builder()
                    .club(club)
                    .clubIntro("")
                    .googleFormUrl("")
                    .recruitmentStatus(RecruitmentStatus.CLOSE)
                    .build();
            clubIntroRepository.save(clubIntro);
            log.debug("동아리 소개 생성 성공: {}", clubIntro.getClubIntro());

            return new ClubCreationResponse(club);
        } else {
            log.warn("관리자 비밀번호 확인 실패");
            throw new AdminException(ExceptionType.ADMIN_PASSWORD_NOT_MATCH);
        }
    }

    // 동아리 삭제(웹)
    public void deleteClub(Long clubId, String adminPw) {
        log.debug("동아리 삭제 요청 시작: clubId = {}", clubId);

        // SecurityContextHolder에서 인증 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomAdminDetails adminDetails = (CustomAdminDetails) authentication.getPrincipal();
        Admin admin = adminDetails.admin();
        log.debug("인증된 관리자: {}", admin.getAdminAccount());

        if (admin.getAdminPw().equals(adminPw)) {
            log.debug("관리자 비밀번호 확인 성공");

            // 동아리 및 관련 종속 엔티티와 S3 파일 삭제
            clubRepository.deleteClubAndDependencies(clubId);

            log.debug("동아리 삭제 성공: clubId = {}", clubId);
        } else {
            log.warn("관리자 비밀번호 확인 실패");
            throw new AdminException(ExceptionType.ADMIN_PASSWORD_NOT_MATCH);
        }
    }
}