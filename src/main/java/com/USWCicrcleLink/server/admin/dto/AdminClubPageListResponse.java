package com.USWCicrcleLink.server.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminClubPageListResponse {
    private List<AdminClubListResponse> content;
    private int totalPages;
    private long totalElements;
    private int currentPage;
}
