package com.USWCicrcleLink.server.admin.notice.repository;

import com.USWCicrcleLink.server.admin.notice.domain.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
    @NonNull
    Page<Notice> findAll(@NonNull Pageable pageable);
}
