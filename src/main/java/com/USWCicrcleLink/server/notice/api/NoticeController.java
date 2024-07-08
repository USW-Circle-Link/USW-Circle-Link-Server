package com.USWCicrcleLink.server.notice.api;

import com.USWCicrcleLink.server.notice.dto.NoticeResponse;
import com.USWCicrcleLink.server.notice.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notice")
public class NoticeController {
    private final NoticeService noticeService;

    @GetMapping("/get-notice/{id}")
    public ResponseEntity<NoticeResponse>getNotice(@PathVariable("id") Long id){
        NoticeResponse noticeResponse = noticeService.getNotice(id);
        return ResponseEntity.ok(noticeResponse);
    }
}
