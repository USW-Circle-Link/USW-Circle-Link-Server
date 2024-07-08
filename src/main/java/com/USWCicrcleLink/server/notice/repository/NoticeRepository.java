package com.USWCicrcleLink.server.notice.repository;

import com.USWCicrcleLink.server.notice.domain.Notice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeRepository extends JpaRepository<Notice,Long> {

}
