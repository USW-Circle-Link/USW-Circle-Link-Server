package com.USWCicrcleLink.server.admin.notice.dto;

import com.USWCicrcleLink.server.admin.notice.domain.Notice;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

/*
    페이징 처리할때 필요
 */
@Component
@RequiredArgsConstructor
public class NoticeListResponseAssembler implements RepresentationModelAssembler<Notice, NoticeListResponse> {

    @Override
    public NoticeListResponse toModel(Notice notice) {
        return NoticeListResponse.builder()
                .noticeId(notice.getNoticeId())
                .noticeTitle(notice.getNoticeTitle())
                .adminName(notice.getAdmin().getAdminName())
                .noticeCreatedAt(notice.getNoticeCreatedAt())
                .build();
    }
}