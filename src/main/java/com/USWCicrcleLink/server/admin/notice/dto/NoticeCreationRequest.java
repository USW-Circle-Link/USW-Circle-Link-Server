package com.USWCicrcleLink.server.admin.notice.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NoticeCreationRequest {
    private String noticeTitle;
    private String noticeContent;

    private List<Integer> photoOrders = new ArrayList<>(); // 사진 순서 목록
}