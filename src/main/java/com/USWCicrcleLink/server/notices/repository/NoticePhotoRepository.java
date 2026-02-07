package com.USWCicrcleLink.server.notices.repository;

import com.USWCicrcleLink.server.notices.domain.Notice;
import com.USWCicrcleLink.server.notices.domain.NoticePhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoticePhotoRepository extends JpaRepository<NoticePhoto, Long> {
    List<NoticePhoto> findByNotice(Notice notice);
}
