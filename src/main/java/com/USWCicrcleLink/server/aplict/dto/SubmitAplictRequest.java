package com.USWCicrcleLink.server.aplict.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
public class SubmitAplictRequest {
    private List<AnswerDto> answers;

    @Getter
    @NoArgsConstructor
    public static class AnswerDto {
        private Long questionId;
        private Long optionId;     // 선택형일 때 값 존재 (없으면 null)
        private String answerText; // 서술형일 때 값 존재 (없으면 null)
    }
}