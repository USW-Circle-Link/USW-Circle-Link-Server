package com.USWCicrcleLink.server.club.form.service;

import com.USWCicrcleLink.server.club.leader.domain.FormQuestion;
import com.USWCicrcleLink.server.club.leader.domain.ClubForm;
import com.USWCicrcleLink.server.club.form.dto.ClubFormResponse;
import com.USWCicrcleLink.server.club.form.repository.ClubFormRepository;
import com.USWCicrcleLink.server.club.clubInfo.repository.ClubInfoRepository;
import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.ClubException;
import com.USWCicrcleLink.server.global.security.details.CustomAdminDetails;
import com.USWCicrcleLink.server.global.security.details.CustomLeaderDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubFormService {
        private final ClubFormRepository clubFormRepository;
        private final ClubInfoRepository clubInfoRepository;

        public ClubFormResponse getQuestionsByClub(UUID clubUUID) {
                // 1. 모집 상태 확인
                var clubInfo = clubInfoRepository.findByClubuuid(clubUUID)
                                .orElseThrow(() -> new ClubException(ExceptionType.CLUB_INFO_NOT_EXISTS));

                // 리더나 어드민은 모집 상태와 무관하게 조회 가능
                if (!isLeaderOrAdminOfClub(clubUUID)) {
                        if (clubInfo.getRecruitmentStatus() == com.USWCicrcleLink.server.club.domain.RecruitmentStatus.CLOSE) {
                                throw new ClubException(ExceptionType.RECRUITMENT_CLOSED);
                        }
                }

                // 2. 최신 폼 가져오기
                List<ClubForm> forms = clubFormRepository.findFormsByClubUUID(clubUUID);
                if (forms.isEmpty()) {
                        throw new ClubException(ExceptionType.CLUB_FORM_NOT_FOUND);
                }
                ClubForm form = forms.get(0);

                List<ClubFormResponse.QuestionResponse> questions = form.getQuestions().stream()
                                .sorted(Comparator.comparingInt(FormQuestion::getSequence))
                                .map(q -> new ClubFormResponse.QuestionResponse(
                                                q.getQuestionId(),
                                                q.getContent(),
                                                q.getType().name(),
                                                q.getSequence(),
                                                q.isRequired(),
                                                q.getOptions().stream()
                                                                .map(o -> new ClubFormResponse.OptionResponse(
                                                                                o.getOptionId(), o.getContent()))
                                                                .toList()))
                                .toList();

                return new ClubFormResponse(form.getFormId(), questions);
        }

        private boolean isLeaderOrAdminOfClub(UUID clubUUID) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication == null || !authentication.isAuthenticated()) {
                        return false;
                }

                Object principal = authentication.getPrincipal();

                if (principal instanceof CustomAdminDetails) {
                        return true;
                }

                if (principal instanceof CustomLeaderDetails leaderDetails) {
                        return leaderDetails.getClubuuid().equals(clubUUID);
                }

                return false;
        }
}
