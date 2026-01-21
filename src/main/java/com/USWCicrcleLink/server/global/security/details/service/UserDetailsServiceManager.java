package com.USWCicrcleLink.server.global.security.details.service;

import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.UserException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceManager {

    private final List<RoleBasedUserDetailsService> userDetailsServices;

    public UserDetails loadUserByUuid(UUID uuid) {
        for (RoleBasedUserDetailsService service : userDetailsServices) {
            try {
                return service.loadUserByUuid(uuid);
            } catch (UserException ignored) {
            }
        }
        throw new UserException(ExceptionType.USER_NOT_EXISTS);
    }
}