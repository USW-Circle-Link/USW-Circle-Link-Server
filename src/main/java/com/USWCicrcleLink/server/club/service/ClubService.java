package com.USWCicrcleLink.server.club.service;

import com.USWCicrcleLink.server.club.domain.Club;
import com.USWCicrcleLink.server.club.repository.ClubRepository;
import com.USWCicrcleLink.server.club.domain.Department;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ClubService {
    private final ClubRepository clubRepository;

    @Transactional(readOnly = true)
    public List<Club> getAllClubs(){
        log.info("모든 동아리 조회");
        List<Club> clubs = clubRepository.findAll();
        if (clubs.isEmpty()) {
            throw new NoSuchElementException("동아리가 없습니다.");
        }
        return clubs;
    }

    @Transactional(readOnly = true)
    public Club getClubById(Long id) {
        log.info("동아리 조회 id: {}", id);
        return clubRepository.findById(id).orElseThrow(() ->
                new NoSuchElementException("해당 ID를 가진 동아리를 찾을 수 없습니다.")
        );
    }

    @Transactional(readOnly = true)
    public List<Club> getClubsByDepartment(Department department) {
        log.info("분과별 동아리 조회: {}", department);
        List<Club> clubs = clubRepository.findByDepartment(department);
        if (clubs.isEmpty()) {
            throw new NoSuchElementException("해당 분과에 속하는 동아리가 없습니다.");
        }
        return clubs;
    }
}