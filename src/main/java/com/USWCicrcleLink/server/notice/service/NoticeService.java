package com.USWCicrcleLink.server.notice.service;

import com.USWCicrcleLink.server.admin.domain.Admin;
import com.USWCicrcleLink.server.admin.repository.AdminRepository;
import com.USWCicrcleLink.server.notice.domain.Notice;
import com.USWCicrcleLink.server.notice.dto.NoticeResponse;
import com.USWCicrcleLink.server.notice.repository.NoticeRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final AdminRepository adminRepository;

    @PostConstruct
    public void initNotice(){
        Admin admin = Admin.builder()
                .adminPw("1234")
                .adminAccount("admin")
                .adminNickname("동아리연합회")
                .build();
        adminRepository.save(admin);

        Notice notice=Notice.builder()
                .admin(admin)
                .noticeTitle("공지사항 제목")
                .noticeContent("안녕하세요 공지사항입니다.")
                .noticeCreatedAt(LocalDateTime.now())
                .noticeUpdatedAt(LocalDateTime.now())
                .build();
        noticeRepository.save(notice);
    }

    public NoticeResponse getNotice(Long noticeId){
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new RuntimeException("noticeId에 해당하는 게시물이 존재하지 않습니다.: " + noticeId));

        NoticeResponse noticeResponse = new NoticeResponse();
        noticeResponse.setNoticeTitle(notice.getNoticeTitle());
        noticeResponse.setNoticeContent((notice.getNoticeContent()));
        noticeResponse.setAdminNickname(notice.getAdmin().getAdminNickname());
        noticeResponse.setNoticeCreatedAt(notice.getNoticeCreatedAt());
        noticeResponse.setNoticeUpdatedAt(notice.getNoticeUpdatedAt());

        return noticeResponse;
    }

}
