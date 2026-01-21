package com.USWCicrcleLink.server.aplict.repository;

import com.USWCicrcleLink.server.aplict.domain.Aplict;
import com.USWCicrcleLink.server.aplict.domain.AplictStatus;

import java.util.List;

public interface AplictRepositoryCustom {
    List<Aplict> findAllWithProfileByClubId(Long clubId, boolean checked);

    List<Aplict> findAllWithProfileByClubIdAndFailed(Long clubId, boolean checked, AplictStatus status);
}
