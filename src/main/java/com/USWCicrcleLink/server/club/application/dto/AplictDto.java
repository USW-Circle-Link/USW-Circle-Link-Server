package com.USWCicrcleLink.server.club.application.dto;

import com.USWCicrcleLink.server.club.application.domain.AplictStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class AplictDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubmitRequest {
        @Valid
        @NotNull
        private List<AnswerRequest> qnaList;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnswerRequest {
        @NotNull
        private Long questionId;
        private Long optionId;
        private List<Long> optionIds; // 체크박스 다중선택용
        private String answerText;
    }

    @Getter
    @AllArgsConstructor
    public static class DetailResponse {
        private UUID aplictUUID;
        private String applicantName;
        private String studentNumber;
        private String department;
        private LocalDateTime submittedAt;
        @Schema(description = "지원 상태", example = "WAIT", allowableValues = { "WAIT", "PASS", "FAIL" })
        private AplictStatus status;
        private List<QnAResponse> qnaList;
    }


    @Getter
    @AllArgsConstructor
    public static class QnAResponse {
        private String question;
        private String answer;
        private Long optionId;
        private String optionContent;
        private List<Long> optionIds; // 체크박스 다중선택 응답용
        private List<String> optionContents; // 체크박스 다중선택 응답용
    }

    @Getter
    @NoArgsConstructor
    public static class UpdateStatusRequest {
        @NotNull
        private AplictStatus status;
    }
}
