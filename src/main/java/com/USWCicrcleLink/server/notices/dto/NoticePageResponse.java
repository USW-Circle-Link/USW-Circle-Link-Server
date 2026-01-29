package com.USWCicrcleLink.server.notices.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NoticePageResponse {
    private List<NoticeListResponse> content;
    private int totalPages;
    private long totalElements;
    private int currentPage;
}

