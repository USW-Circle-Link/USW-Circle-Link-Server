package com.USWCicrcleLink.server.global.security.details.service;

import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.UserException;
import com.USWCicrcleLink.server.global.security.jwt.domain.Role;
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
    private final CustomUserDetailsService customUserDetailsService;
    private final CustomLeaderDetailsService customLeaderDetailsService;
    private final CustomAdminDetailsService customAdminDetailsService;

    public UserDetails loadUserByUuid(UUID uuid) {
        for (RoleBasedUserDetailsService service : userDetailsServices) {
            try {
                return service.loadUserByUuid(uuid);
            } catch (Exception ignored) {
                // 특정 권한의 정보가 없을 경우 다음 서비스 시도
            }
        }
        throw new UserException(ExceptionType.USER_NOT_EXISTS);
    }


    public UserDetails loadUserByUuidAndRole(UUID uuid, Role role) {
        return switch (role) {
            case USER -> customUserDetailsService.loadUserByUuid(uuid);
            case LEADER -> customLeaderDetailsService.loadUserByUuid(uuid);
            case ADMIN -> customAdminDetailsService.loadUserByUuid(uuid);
            default -> throw new UserException(ExceptionType.USER_NOT_EXISTS);
        };
    }
}