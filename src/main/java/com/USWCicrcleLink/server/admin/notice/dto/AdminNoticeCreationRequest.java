package com.USWCicrcleLink.server.admin.notice.dto;

import com.USWCicrcleLink.server.global.validation.Sanitize;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AdminNoticeCreationRequest {

    @NotBlank(message = "공지사항 제목은 비워둘 수 없습니다.")
    @Size(max = 200, message = "공지사항 제목은 최대 200자까지 입력 가능합니다.")
    @Sanitize
    private String noticeTitle;

    @NotBlank(message = "공지사항 내용은 비워둘 수 없습니다.")
    @Size(max = 3000, message = "공지사항 내용은 최대 3000자까지 입력 가능합니다.")
    @Sanitize
    private String noticeContent;

    @Size(max = 5, message = "사진은 최대 5장까지 업로드 가능합니다.")
    private List<@Min(1) @Max(5) Integer> photoOrders;
}