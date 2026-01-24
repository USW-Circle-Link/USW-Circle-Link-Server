package com.USWCicrcleLink.server.clubLeader.service;

import com.USWCicrcleLink.server.club.club.repository.ClubRepository;
import com.USWCicrcleLink.server.clubLeader.domain.*;
import com.USWCicrcleLink.server.club.club.domain.Club;
import com.USWCicrcleLink.server.clubLeader.dto.FormDto;
import com.USWCicrcleLink.server.clubLeader.repository.ClubFormRepository;
import com.USWCicrcleLink.server.global.exception.GlobalExceptionHandler;
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


        Club club = clubRepository.findByClubUUID(clubUUID)
                .orElseThrow(() -> new EntityNotFoundException("해당 동아리를 찾을 수 없습니다."));

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("마감일은 시작일보다 빠를 수 없습니다.");
        }

        if (request.getQuestions() == null || request.getQuestions().isEmpty()) {
            throw new IllegalArgumentException("최소 1개 이상의 질문을 등록해야 합니다.");
        }

        ClubForm form = ClubForm.builder()
                .club(club)
                .title(request.getTitle())
                .description(request.getDescription())
                .startAt(request.getStartDate())
                .endAt(request.getEndDate())
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

    public void updateStatus(UUID clubUUID, Long formId, FormDto.UpdateStatusRequest request) {

        ClubForm form = formRepository.findById(formId)
                .orElseThrow(() -> new EntityNotFoundException("해당 지원서를 찾을 수 없습니다. ID=" + formId));

        // uuid 다를 시 변경 불가
        if (!form.getClub().getClubUUID().equals(clubUUID)) {
            throw new IllegalArgumentException("해당 동아리의 지원서가 아니므로 수정할 수 없습니다.");
        }

        form.updateStatus(request.getStatus());
    }
}