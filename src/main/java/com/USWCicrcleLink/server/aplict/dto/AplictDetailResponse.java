package com.USWCicrcleLink.server.aplict.dto;

import com.USWCicrcleLink.server.aplict.domain.Aplict;
import com.USWCicrcleLink.server.profile.domain.Profile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AplictDetailResponse {
    private Long aplictId;
    private ApplicantInfo applicant;
    private String status;
    private boolean isRead;
    private LocalDateTime submittedAt;
    private List<QnA> answers;

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ApplicantInfo {
        private String name;
        private String studentId;
        private String department;
        private String phone;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class QnA {
        private Long questionId;
        private String question;
        private String type;
        private String answer;
    }

    public static AplictDetailResponse from(Aplict aplict) {
        Profile profile = aplict.getProfile();

        ApplicantInfo applicantInfo = ApplicantInfo.builder()
                .name(profile.getUserName())
                .studentId(profile.getStudentNumber())
                .department(profile.getMajor())
                .phone(profile.getUserHp())
                .build();

        // TODO: Aplict 엔티티에 answers 관계 추가 필요
        // List<QnA> qnaList = aplict.getAnswers().stream()
        // .map(ans -> {
        // String answerContent = (ans.getOption() != null)
        // ? ans.getOption().getContent()
        // : ans.getAnswerText();
        //
        // return QnA.builder()
        // .questionId(ans.getQuestion().getQuestionId())
        // .question(ans.getQuestion().getContent())
        // .type(ans.getQuestion().getType().name())
        // .answer(answerContent)
        // .build();
        // })
        // .collect(Collectors.toList());
        List<QnA> qnaList = List.of(); // 임시로 빈 리스트 반환

        return AplictDetailResponse.builder()
                .aplictId(aplict.getAplictId())
                .applicant(applicantInfo)
                .status(aplict.getAplictStatus().name())
                .isRead(aplict.isChecked())
                .submittedAt(aplict.getSubmittedAt())
                .answers(qnaList)
                .build();
    }
}