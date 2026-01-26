package com.USWCicrcleLink.server.club.form.dto;

import java.util.List;

public record ClubFormResponse(
        Long formId,
        String title,
        List<QuestionResponse> questions
) {
    public record QuestionResponse(
            Long questionId,
            String content,
            String type,
            int sequence,
            boolean required,
            List<OptionResponse> options
    ) {}

    public record OptionResponse(
            Long optionId,
            String content
    ) {}
}