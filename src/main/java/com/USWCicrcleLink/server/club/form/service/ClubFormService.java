package com.USWCicrcleLink.server.club.form.service;

import com.USWCicrcleLink.server.club.leader.domain.FormQuestion;
import com.USWCicrcleLink.server.club.leader.domain.ClubForm;
import com.USWCicrcleLink.server.club.form.dto.ClubFormResponse;
import com.USWCicrcleLink.server.club.form.repository.ClubFormRepository;
import com.USWCicrcleLink.server.club.clubInfo.repository.ClubInfoRepository;
import lombok.RequiredArgsConstructor;
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
                                .orElseThrow(() -> new IllegalArgumentException("동아리 소개 정보를 찾을 수 없습니다."));

                if (clubInfo.getRecruitmentStatus() == com.USWCicrcleLink.server.club.domain.RecruitmentStatus.CLOSE) {
                        throw new IllegalArgumentException("현재 모집 기간이 아닙니다.");
                }

                // 2. 최신 폼 가져오기
                List<ClubForm> forms = clubFormRepository.findFormsByClubUUID(clubUUID);
                if (forms.isEmpty()) {
                        throw new IllegalArgumentException("등록된 모집 양식이 없습니다.");
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
}
