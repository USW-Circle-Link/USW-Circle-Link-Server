package com.USWCicrcleLink.server.club.form.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ClubFormResponse {
    private Long formId;
    private String title;
    private List<QuestionResponse> questions;

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class QuestionResponse {
        private Long questionId;
        private String content;
        private String type;
        private int sequence;
        private boolean required;
        private List<OptionResponse> options;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OptionResponse {
        private Long optionId;
        private String content;
    }
}
