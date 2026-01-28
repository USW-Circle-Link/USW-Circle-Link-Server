package com.USWCicrcleLink.server.user.service;

import com.USWCicrcleLink.server.notices.repository.NoticeRepository;
import com.USWCicrcleLink.server.user.dto.MyNoticeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class MyNoticeService {
    private final NoticeRepository noticeRepository;

    // 공지사항 리스트 조회
    public List<MyNoticeResponse> getNotices() {
        return noticeRepository.findAll().stream()
                .map(MyNoticeResponse::from)
                .collect(Collectors.toList());
    }
}
