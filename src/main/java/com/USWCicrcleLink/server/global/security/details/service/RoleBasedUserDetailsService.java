package com.USWCicrcleLink.server.global.security.details.service;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.UUID;

public interface RoleBasedUserDetailsService {
    UserDetails loadUserByUuid(UUID uuid);
}
