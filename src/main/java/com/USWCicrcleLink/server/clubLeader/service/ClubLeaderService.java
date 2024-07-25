package com.USWCicrcleLink.server.clubLeader.service;

import com.USWCicrcleLink.server.club.club.domain.Club;
import com.USWCicrcleLink.server.club.club.domain.ClubMembers;
import com.USWCicrcleLink.server.club.club.domain.RecruitmentStatus;
import com.USWCicrcleLink.server.club.club.repository.ClubMembersRepository;
import com.USWCicrcleLink.server.club.club.repository.ClubRepository;
import com.USWCicrcleLink.server.club.clubIntro.domain.ClubIntro;
import com.USWCicrcleLink.server.club.clubIntro.repository.ClubIntroRepository;
import com.USWCicrcleLink.server.clubLeader.domain.Leader;
import com.USWCicrcleLink.server.clubLeader.dto.*;
import com.USWCicrcleLink.server.clubLeader.repository.LeaderRepository;
import com.USWCicrcleLink.server.global.response.ApiResponse;
import com.USWCicrcleLink.server.global.util.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ClubLeaderService {
    private final ClubRepository clubRepository;
    private final ClubIntroRepository clubIntroRepository;
    private final LeaderRepository leaderRepository;
    private final ClubMembersRepository clubMembersRepository;
    private final FileUploadService fileUploadService;

    // 대표 사진 경로
    @Value("${file.mainPhoto-dir}")
    private String mainPhotoDir;
    @Value("${file.introPhoto-dir}")
    private String introPhotoDir;

    // 동아리 기본 정보 조회
    @Transactional(readOnly = true)
    public ApiResponse<ClubInfoResponse> getClubInfo(LeaderToken token) {
        // 토큰 적용, 예외 처리 시 변경
        Leader leader = leaderRepository.findByLeaderUUID(token.getLeaderUUID())
                .orElseThrow(() -> new IllegalArgumentException("유효한 동아리 회장이 아닙니다."));
        log.info("동아리 회장 조회 결과: {}", leader);

        // 동아리 조회
        Club club = clubRepository.findById(leader.getClub().getClubId())
                .orElseThrow(() -> new IllegalArgumentException("유효한 동아리 아닙니다."));
        log.info("동아리 조회 결과: {}", club);

        ClubInfoResponse clubInfoResponse = new ClubInfoResponse(
                club.getMainPhotoPath(),
                club.getClubName(),
                club.getLeaderName(),
                club.getLeaderHp(),
                club.getKatalkID(),
                club.getClubInsta()
        );

        return new ApiResponse<>("동아리 기본 정보 조회 완료", clubInfoResponse);
    }

    // 동아리 기본 정보 변경
    public void updateClubInfo(LeaderToken token, ClubInfoRequest clubInfoRequest) throws IOException {

        // 토큰 적용, 예외 처리 시 변경
        Leader leader = leaderRepository.findByLeaderUUID(token.getLeaderUUID())
                .orElseThrow(() -> new IllegalArgumentException("유효한 동아리 회장이 아닙니다."));
        log.info("동아리 회장 조회 결과: {}", leader);

        // 동아리 조회
        Club club = clubRepository.findById(leader.getClub().getClubId())
                .orElseThrow(() -> new IllegalArgumentException("유효한 동아리 아닙니다."));
        log.info("동아리 조회 결과: {}", club);

        // 사진 파일 업로드 과정
        createMainPhotoDir();// 사진 파일 디렉터리 없는 경우 생성

        // 파일 저장 경로
        String mainPhotoPath = fileUploadService.saveFile(clubInfoRequest.getMainPhoto(), club.getMainPhotoPath(), mainPhotoDir);

        // 동아리 정보 변경
        club.updateClubInfo(mainPhotoPath, clubInfoRequest.getLeaderName(), clubInfoRequest.getLeaderHp(),
                clubInfoRequest.getKatalkID(), clubInfoRequest.getClubInsta());
//        , clubInfoRequest.getchatRoomURL);

        clubRepository.save(club);
        log.info("동아리 기본 정보 변경 완료: {}", club.getClubName());
    }

    // 동아리 소개 변경
    public void updateClubIntro(LeaderToken token, ClubIntroRequest clubIntroRequest) throws IOException {

        // 토큰 적용, 예외 처리 시 변경
        Leader leader = leaderRepository.findByLeaderUUID(token.getLeaderUUID())
                .orElseThrow(() -> new IllegalArgumentException("유효한 동아리 회장이 아닙니다."));
        log.info("동아리 회장 조회 결과: {}", leader);

        // 동아리 조회
        Club club = clubRepository.findById(leader.getClub().getClubId())
                .orElseThrow(() -> new IllegalArgumentException("유효한 동아리 아닙니다."));
        log.info("동아리 조회 결과: {}", club);

        // 사진 파일 업로드 과정
        createIntroPhotoDir();// 사진 파일 디렉터리 없는 경우 생성

        // 기존 파일 경로가 있는지 확인
        ClubIntro existingClubIntro = clubIntroRepository.findByClub(club)
                .orElseThrow(() -> new IllegalArgumentException("유효한 동아리 소개가 아닙니다."));

        // 파일 있나 ? 덮어쓰기 : 비워두기
        String introPhotoPath = fileUploadService.saveFile(clubIntroRequest.getIntroPhoto(),
                existingClubIntro.getClubIntroPhotoPath(), introPhotoDir);

        String additionalPhotoPath1 = fileUploadService.saveFile(clubIntroRequest.getAdditionalPhoto1(),
                existingClubIntro.getAdditionalPhotoPath1(), introPhotoDir);

        String additionalPhotoPath2 = fileUploadService.saveFile(clubIntroRequest.getAdditionalPhoto2(),
                existingClubIntro.getAdditionalPhotoPath2(), introPhotoDir);

        String additionalPhotoPath3 = fileUploadService.saveFile(clubIntroRequest.getAdditionalPhoto3(),
                existingClubIntro.getAdditionalPhotoPath3(), introPhotoDir);

        String additionalPhotoPath4 = fileUploadService.saveFile(clubIntroRequest.getAdditionalPhoto4(),
                existingClubIntro.getAdditionalPhotoPath4(), introPhotoDir);

        // 동아리 소개 저장
        existingClubIntro.updateClubIntro(club, clubIntroRequest.getClubIntro(), clubIntroRequest.getGoogleFormUrl(),
                introPhotoPath, additionalPhotoPath1, additionalPhotoPath2,
                additionalPhotoPath3, additionalPhotoPath4);

        clubIntroRepository.save(existingClubIntro);
        log.info("동아리 소개 저장 완료: {}", existingClubIntro);
    }

    // 사진 파일 저장 디렉터리 없는 경우 생성
    private void createMainPhotoDir() throws IOException {
        Files.createDirectories(Paths.get(mainPhotoDir));
    }

    private void createIntroPhotoDir() throws IOException {
        Files.createDirectories(Paths.get(introPhotoDir));
    }

    // 동아리 모집 상태 변경
    public RecruitmentStatus toggleRecruitmentStatus(LeaderToken token) {

        // 토큰 적용, 예외 처리 시 변경
        Leader leader = leaderRepository.findByLeaderUUID(token.getLeaderUUID())
                .orElseThrow(() -> new IllegalArgumentException("유효한 동아리 회장이 아닙니다."));
        log.info("동아리 회장 조회 결과: {}", leader);

        // 동아리 조회
        Club club = clubRepository.findById(leader.getClub().getClubId())
                .orElseThrow(() -> new IllegalArgumentException("유효한 동아리 아닙니다."));
        log.info("동아리 조회 결과: {}", club);

        ClubIntro clubIntro = clubIntroRepository.findByClub(club)
                .orElseThrow(() -> new IllegalArgumentException("유효한 동아리 소개가 아닙니다."));
        log.info("동아리 소개 조회 결과: {}", clubIntro);

        // 모집 상태 현재와 반전
        club.toggleRecruitmentStatus();

        return clubIntro.getRecruitmentStatus();
    }

    // 소속 동아리원 조회
    @Transactional(readOnly = true)
    public ApiResponse<List<ClubMembersResponse>> findClubMembers(LeaderToken token) {

        // 토큰 적용, 예외 처리 시 변경
        Leader leader = leaderRepository.findByLeaderUUID(token.getLeaderUUID())
                .orElseThrow(() -> new IllegalArgumentException("유효한 동아리 회장이 아닙니다."));
        log.info("동아리 회장 조회 결과: {}", leader);

        // 동아리 조회
        Club club = clubRepository.findById(leader.getClub().getClubId())
                .orElseThrow(() -> new IllegalArgumentException("유효한 동아리 아닙니다."));
        log.info("동아리 조회 결과: {}", club);

        // 해당 동아리원 조회(성능 비교)
//        List<ClubMembers> findClubMembers = clubMembersRepository.findByClub(club); // 일반
        List<ClubMembers> findClubMembers = clubMembersRepository.findAllWithProfile(club.getClubId()); // 성능

        // 동아리원과 프로필 조회
        List<ClubMembersResponse> memberProfiles = findClubMembers.stream()
                .map(cm -> new ClubMembersResponse(
                        cm.getClubMemberId(),
                        cm.getProfile()
                ))
                .collect(toList());

        return new ApiResponse<>("소속 동아리원 조회 완료", memberProfiles);
    }

    // 소속 동아리원 삭제
    public ApiResponse deleteClubMember(Long clubMemberId, LeaderToken token) {

        // 토큰 적용, 예외 처리 시 변경
        Leader leader = leaderRepository.findByLeaderUUID(token.getLeaderUUID())
                .orElseThrow(() -> new IllegalArgumentException("유효한 동아리 회장이 아닙니다."));
        log.info("동아리 회장 조회 결과: {}", leader);

        // 동아리 조회
        Club club = clubRepository.findById(leader.getClub().getClubId())
                .orElseThrow(() -> new IllegalArgumentException("유효한 동아리 아닙니다."));
        log.info("동아리 조회 결과: {}", club);

        // 동아리원 삭제
        clubMembersRepository.deleteById(clubMemberId);
        return new ApiResponse<>("동아리원 삭제 완료");
    }

}