package com.USWCicrcleLink.server.clubLeader.service;

import com.USWCicrcleLink.server.club.club.repository.ClubRepository;
import com.USWCicrcleLink.server.clubLeader.domain.*;
import com.USWCicrcleLink.server.club.club.domain.Club;
import com.USWCicrcleLink.server.clubLeader.dto.FormDto;
import com.USWCicrcleLink.server.clubLeader.repository.FormRepository;
import com.USWCicrcleLink.server.global.exception.GlobalExceptionHandler;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class FormService {

    private final FormRepository formRepository;
    private final ClubRepository clubRepository;

    //폼 생성 (통합)
    public Long createForm(Long clubId, FormDto.CreateRequest request) {

        //동아리 존재 여부 (404 Not Found)
        // GlobalExceptionHandler의 handleEntityNotFoundException가 잡아서 처리함
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new EntityNotFoundException("해당 동아리를 찾을 수 없습니다. ID=" + clubId));

        //날짜 논리 검증 (400 Bad Request)
        // GlobalExceptionHandler의 handleIllegalArgumentException가 잡아서 처리함
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("마감일은 시작일보다 빠를 수 없습니다.");
        }

        //질문 최소 개수 검증 (DTO @Size로도 막지만 더블 체크)
        if (request.getQuestions() == null || request.getQuestions().isEmpty()) {
            throw new IllegalArgumentException("최소 1개 이상의 질문을 등록해야 합니다.");
        }

        // 1. 폼 엔티티 생성
        ClubForm form = ClubForm.builder()
                .club(club)
                .title(request.getTitle())
                .description(request.getDescription())
                .startAt(request.getStartDate())
                .endAt(request.getEndDate())
                .createdBy(1L) //
                .build();

        // 2. 질문 및 옵션 조립 (Cascade로 인해 자동 저장됨)
        for (FormDto.QuestionRequest qReq : request.getQuestions()) {

            // 질문 생성
            FormQuestion question = FormQuestion.builder()
                    .sequence(qReq.getSequence())
                    .type(qReq.getType())
                    .content(qReq.getContent())
                    .required(qReq.isRequired())
                    .build();

            // 옵션 생성 (옵션이 있는 질문인 경우만)
            if (qReq.getOptions() != null && !qReq.getOptions().isEmpty()) {
                for (FormDto.OptionRequest oReq : qReq.getOptions()) {
                    question.addOption(FormQuestionOption.builder()
                            .sequence(oReq.getSequence())
                            .content(oReq.getContent())
                            .value(oReq.getValue())
                            .build());
                }
            }

            // 폼에 질문 추가 (연관관계 편의 메서드 활용 권장)
            form.addQuestion(question);
        }

        // 3. 저장 (폼만 저장하면 하위 데이터 싹 다 저장됨)
        return formRepository.save(form).getFormId();
    }

    //상태 변경
    public void updateStatus(Long clubId, Long formId, FormDto.UpdateStatusRequest request) {

        // 지원서 존재 여부 (404 Not Found)
        ClubForm form = formRepository.findById(formId)
                .orElseThrow(() -> new EntityNotFoundException("해당 지원서를 찾을 수 없습니다. ID=" + formId));

        //권한/소유권 검증 (400 Bad Request)
        // 내 동아리의 폼이 아닌 경우
        if (!form.getClub().getClubId().equals(clubId)) {
            throw new IllegalArgumentException("해당 동아리의 지원서가 아니므로 수정할 수 없습니다.");
        }

        // 상태 변경
        form.updateStatus(request.getStatus());
        // @Transactional에 의해 자동 Dirty Checking -> Update Query 실행
    }
}