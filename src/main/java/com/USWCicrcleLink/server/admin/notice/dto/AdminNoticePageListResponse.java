package com.USWCicrcleLink.server.admin.notice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminNoticePageListResponse {
    private List<AdminNoticeListResponse> content;
    private int totalPages;
    private long totalElements;
    private int currentPage;
}
