package com.USWCicrcleLink.server.club.form.service;

import com.USWCicrcleLink.server.club.leader.domain.FormQuestion;
import com.USWCicrcleLink.server.club.form.dto.ClubFormResponse;
import com.USWCicrcleLink.server.club.form.repository.ClubFormRepository;
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

        public ClubFormResponse getQuestionsByClub(UUID clubUUID) {
                var form = clubFormRepository.findActiveFormByClubUUID(clubUUID)
                                .orElseThrow(() -> new IllegalArgumentException("현재 모집 중인 양식이 없습니다."));

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

                return new ClubFormResponse(form.getFormId(), form.getTitle(), questions);
        }
}
