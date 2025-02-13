package com.USWCicrcleLink.server.global.security.util;

import com.USWCicrcleLink.server.user.domain.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public record CustomUserDetails(User user, List<UUID> clubUUIDs) implements UserDetails {

    public List<UUID> getClubUUIDs() {
        return clubUUIDs;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(() -> "ROLE_" + user.getRole().name());
    }

    @Override
    public String getPassword() {
        return user.getUserPw();
    }

    @Override
    public String getUsername() {
        return user.getUserUUID().toString();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // 계정이 만료되지 않았는지 여부 반환
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // 계정이 잠기지 않았는지 여부 반환
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 자격 증명이 만료되지 않았는지 여부 반환
    }

    @Override
    public boolean isEnabled() {
        return true; // 계정이 활성화되었는지 여부 반환
    }
}
