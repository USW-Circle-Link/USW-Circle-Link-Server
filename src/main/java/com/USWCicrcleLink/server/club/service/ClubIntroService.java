package com.USWCicrcleLink.server.club.service;

import com.USWCicrcleLink.server.club.domain.ClubIntro;
import com.USWCicrcleLink.server.club.domain.RecruitmentStatus;
import com.USWCicrcleLink.server.club.repository.ClubIntroRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ClubIntroService {
    private final ClubIntroRepository clubIntroRepository;

    @Transactional(readOnly = true)
    public ClubIntro getClubIntroByClubId(Long clubId) {
        log.info("동아리 소개 조회 id: {}", clubId);
        ClubIntro clubIntro = clubIntroRepository.findByClubClubId(clubId).orElseThrow(() ->
                new NoSuchElementException("해당 클럽 ID에 대한 소개를 찾을 수 없습니다.")
        );
        clubIntro.setRecruitmentStatus(clubIntro.getRecruitmentStatus());
        return clubIntro;
    }

    public void applyToClub(Long clubId) {
        ClubIntro clubIntro = clubIntroRepository.findByClubClubId(clubId).orElseThrow(() ->
                new NoSuchElementException("해당 클럽 ID에 대한 소개를 찾을 수 없습니다.")
        );
        if (!clubIntro.getRecruitmentStatus().isOpen()) {
            throw new IllegalStateException("현재 모집 중이 아닙니다.");
        }
        log.info("지원 요청 처리: clubId {}", clubId);
    }
}