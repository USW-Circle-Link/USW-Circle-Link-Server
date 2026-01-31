package com.USWCicrcleLink.server.club.leader.service;

import com.USWCicrcleLink.server.club.domain.Club;
import com.USWCicrcleLink.server.club.repository.ClubRepository;
import com.USWCicrcleLink.server.club.leader.domain.ClubForm;
import com.USWCicrcleLink.server.club.form.repository.ClubFormRepository;
import com.USWCicrcleLink.server.club.leader.domain.FormQuestion;
import com.USWCicrcleLink.server.club.leader.domain.FormQuestionOption;
import com.USWCicrcleLink.server.club.leader.dto.FormDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class FormService {

    private final ClubFormRepository formRepository;
    private final ClubRepository clubRepository;

    // 폼 생성 (통합)
    public Long createForm(UUID clubUUID, FormDto.CreateRequest request) {

        Club club = clubRepository.findByClubuuid(clubUUID)
                .orElseThrow(() -> new EntityNotFoundException("해당 동아리를 찾을 수 없습니다."));

        if (request.getQuestions() == null || request.getQuestions().isEmpty()) {
            throw new IllegalArgumentException("최소 1개 이상의 질문을 등록해야 합니다.");
        }

        ClubForm form = ClubForm.builder()
                .club(club)
                .description(request.getDescription())
                .createdBy(1L)
                .build();

        for (FormDto.QuestionRequest qReq : request.getQuestions()) {
            FormQuestion question = FormQuestion.builder()
                    .sequence(qReq.getSequence())
                    .type(qReq.getType())
                    .content(qReq.getContent())
                    .required(qReq.isRequired())
                    .build();

            if (qReq.getOptions() != null && !qReq.getOptions().isEmpty()) {
                for (FormDto.OptionRequest oReq : qReq.getOptions()) {
                    question.addOption(FormQuestionOption.builder()
                            .sequence(oReq.getSequence())
                            .content(oReq.getContent())
                            .value(oReq.getValue())
                            .build());
                }
            }
            form.addQuestion(question);
        }

        return formRepository.save(form).getFormId();
    }

    @Transactional(readOnly = true)
    public ClubForm getForm(Long formId) {
        return formRepository.findById(formId)
                .orElseThrow(() -> new EntityNotFoundException("해당 지원서를 찾을 수 없습니다. ID=" + formId));
    }

}
