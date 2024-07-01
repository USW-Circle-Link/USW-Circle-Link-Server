package com.USWCicrcleLink.server.aplict.service;

import com.USWCicrcleLink.server.aplict.domain.Aplict;
import com.USWCicrcleLink.server.aplict.domain.AplictStatus;
import com.USWCicrcleLink.server.aplict.dto.AplictRequest;
import com.USWCicrcleLink.server.aplict.repository.AplictRepository;
import com.USWCicrcleLink.server.club.repository.ClubRepository;
import com.USWCicrcleLink.server.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class AplictService {
    private final AplictRepository aplictRepository;
    private final ClubRepository clubRepository;
    private final ProfileRepository profileRepository;

    //지원서 제출
    public void submitAplict(AplictRequest request) {
        Aplict aplict = Aplict.builder()
                .profile(profileRepository.findById(request.getProfileId()).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다.")))
                .club(clubRepository.findById(request.getClubId()).orElseThrow(() -> new IllegalArgumentException("동아리를 찾을 수 없습니다.")))
                .aplictGoogleFormUrl(request.getAplictGoogleFormUrl())
                .submittedAt(LocalDateTime.now())
                .status(AplictStatus.WAIT)
                .build();

        aplictRepository.save(aplict);
    }

    //해당동아리 지원서 조회
    public List<Aplict> getAplictByClubId(Long clubId) {
        return aplictRepository.findByClub(clubRepository.findById(clubId).orElseThrow(() -> new IllegalArgumentException("동아리를 찾을 수 없습니다.")));
    }
}
