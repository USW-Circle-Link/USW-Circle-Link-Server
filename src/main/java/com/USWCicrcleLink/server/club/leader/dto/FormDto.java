package com.USWCicrcleLink.server.club.leader.dto;

import com.USWCicrcleLink.server.club.leader.domain.QuestionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class FormDto {

    @Getter
    @NoArgsConstructor
    public static class CreateRequest {

        @Size(max = 500, message = "설명은 500자 이내여야 합니다.")
        private String description;

        @Valid // 질문 리스트 내부 객체까지 검사
        @NotNull(message = "질문 목록은 필수입니다.")
        @Size(min = 1, message = "최소 1개 이상의 질문을 등록해야 합니다.") // 질문 0개 방지
        private List<QuestionRequest> questions;
    }

    @Getter
    @NoArgsConstructor
    public static class QuestionRequest {

        @NotNull(message = "질문 순서(sequence)는 필수입니다.")
        private Integer sequence;

        @NotNull(message = "질문 유형(type)은 필수입니다.")
        private QuestionType type;

        @NotBlank(message = "질문 내용은 필수입니다.")
        @Size(max = 200, message = "질문 내용은 200자 이내여야 합니다.")
        private String content;

        private boolean required; //

        @Valid // 옵션 리스트 내부 객체까지 검사
        private List<OptionRequest> options;
    }

    @Getter
    @NoArgsConstructor
    public static class OptionRequest {

        @NotNull(message = "옵션 순서(sequence)는 필수입니다.")
        private Integer sequence;

        @NotBlank(message = "옵션 내용은 필수입니다.")
        @Size(max = 50, message = "옵션 내용은 50자 이내여야 합니다.")
        private String content;

        private String value; // 선택된 값 (null 허용할지 여부에 따라 @NotNull 추가 가능)
    }

}
