package com.USWCicrcleLink.server.club.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClubSearchCondition {
    private Boolean open;
    private String filter;
    private List<UUID> categoryUUIDs;
    private Boolean includeAdminInfo;

    public void setInclude_admin_info(Boolean includeAdminInfo) {
        this.includeAdminInfo = includeAdminInfo;
    }
}
