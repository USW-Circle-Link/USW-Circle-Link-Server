package com.USWCicrcleLink.server.global.security.util;

import com.USWCicrcleLink.server.clubLeader.domain.Leader;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

public record CustomLeaderDetails(Leader leader, UUID clubUUID) implements UserDetails {

    public UUID getClubUUID() {
        return clubUUID;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(() -> "ROLE_" + leader.getRole().name());
    }

    @Override
    public String getPassword() {
        return leader.getLeaderPw();
    }

    @Override
    public String getUsername() {
        return leader.getLeaderUUID().toString();
    }

    public Boolean getIsAgreedTerms() { return leader.isAgreedTerms();}

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
