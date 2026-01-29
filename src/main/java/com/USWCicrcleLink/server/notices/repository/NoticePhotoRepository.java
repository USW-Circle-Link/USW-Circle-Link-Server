package com.USWCicrcleLink.server.notices.repository;

import com.USWCicrcleLink.server.notices.domain.Notice;
import com.USWCicrcleLink.server.notices.domain.NoticePhoto;
import com.USWCicrcleLink.server.club.clubIntro.domain.ClubIntroPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoticePhotoRepository extends JpaRepository<NoticePhoto, Long> {
    List<NoticePhoto> findByNotice(Notice notice);
}
