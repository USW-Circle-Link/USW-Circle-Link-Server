package com.USWCicrcleLink.server.club.application.repository;

import com.USWCicrcleLink.server.club.application.domain.Aplict;
import com.USWCicrcleLink.server.club.application.domain.AplictStatus;

import java.util.List;

public interface AplictRepositoryCustom {
    List<Aplict> findAllWithProfileByClubIdAndStatus(Long clubId, AplictStatus status);

    List<Aplict> findAllWithProfileByClubId(Long clubId);
}
