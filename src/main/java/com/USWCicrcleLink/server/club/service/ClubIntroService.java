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
        return clubIntroRepository.findByClubClubId(clubId).orElseThrow(() ->
                new NoSuchElementException("해당 클럽 ID에 대한 소개를 찾을 수 없습니다.")
        );
    }
}