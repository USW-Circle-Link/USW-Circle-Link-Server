package com.USWCicrcleLink.server.club.application.repository;

import com.USWCicrcleLink.server.club.application.domain.Aplict;
import com.USWCicrcleLink.server.club.application.domain.AplictStatus;

import java.util.List;

public interface AplictRepositoryCustom {
    List<Aplict> findAllWithProfileByClubId(Long clubId, boolean checked);

    List<Aplict> findAllWithProfileByClubIdAndFailed(Long clubId, boolean checked, AplictStatus status);
}

