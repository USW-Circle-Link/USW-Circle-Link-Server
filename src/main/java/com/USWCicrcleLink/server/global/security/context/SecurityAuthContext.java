package com.USWCicrcleLink.server.global.security.context;

import com.USWCicrcleLink.server.global.security.details.CustomLeaderDetails;
import com.USWCicrcleLink.server.global.security.details.CustomUserDetails;
import com.USWCicrcleLink.server.user.domain.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SecurityAuthContext implements AuthContext {

    @Override
    public UUID getCurrentUUID() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomLeaderDetails principal = (CustomLeaderDetails) authentication.getPrincipal();
        return principal.getClubUUID();
    }

    @Override
    public User getUserByAuth() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.user();
    }


}
