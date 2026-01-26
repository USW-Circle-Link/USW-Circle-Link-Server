package com.USWCicrcleLink.server.clubLeader.domain;

import lombok.Getter;

@Getter
public enum QuestionType {
    LONG_TEXT(RenderType.TEXT),
    SHORT_TEXT(RenderType.TEXT),
    RADIO(RenderType.RADIO),
    DROPDOWN(RenderType.SELECT),
    CHECKBOX(RenderType.CHECKBOX);

    private final RenderType defaultRenderType;

    QuestionType(RenderType defaultRenderType) {
        this.defaultRenderType = defaultRenderType;
    }
}