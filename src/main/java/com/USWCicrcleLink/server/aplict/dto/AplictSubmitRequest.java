package com.USWCicrcleLink.server.aplict.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AplictSubmitRequest {
    private Object applicant; // 요청 Body 포맷 맞춤 (실제 처리는 Token 사용)
    private List<AplictAnswerRequest> answers;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AplictAnswerRequest {
        private Long questionId;
        private String answerContent;
    }
}
