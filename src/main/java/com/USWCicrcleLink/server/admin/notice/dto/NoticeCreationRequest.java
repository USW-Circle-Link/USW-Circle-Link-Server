package com.USWCicrcleLink.server.admin.notice.dto;

import com.USWCicrcleLink.server.admin.admin.domain.Admin;
import com.USWCicrcleLink.server.admin.notice.domain.Notice;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NoticeCreationRequest {

    @NotBlank(message = "공지사항 제목은 비워둘 수 없습니다.")
    @Size(max = 100, message = "공지사항 제목은 최대 100자까지 입력 가능합니다.")
    private String noticeTitle;

    @NotBlank(message = "공지사항 내용은 비워둘 수 없습니다.")
    @Size(max = 3000, message = "공지사항 내용은 최대 3000자까지 입력 가능합니다.")
    private String noticeContent;

    @Size(max = 5, message = "사진은 최대 5장까지 업로드 가능합니다.")
    private List<@Min(1) @Max(5) Integer> photoOrders;  // 1~5 범위 내의 순서 제한

    // DTO에서 엔티티로 변환 메서드
    public Notice toEntity(Admin admin) {
        return Notice.builder()
                .noticeTitle(this.noticeTitle)
                .noticeContent(this.noticeContent)
                .noticeCreatedAt(LocalDateTime.now())  // 생성 시점 자동 설정
                .admin(admin)  // Admin 엔티티 전달
                .build();
    }
}