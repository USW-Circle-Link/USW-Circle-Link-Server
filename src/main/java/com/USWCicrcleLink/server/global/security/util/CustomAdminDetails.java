package com.USWCicrcleLink.server.global.security.util;

import com.USWCicrcleLink.server.admin.admin.domain.Admin;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public record CustomAdminDetails(Admin admin) implements UserDetails {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(() -> "ROLE_" + admin.getRole().name());
    }

    @Override
    public String getPassword() {
        return admin.getAdminPw();
    }

    @Override
    public String getUsername() {
        return admin.getAdminUUID().toString();
    }

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
