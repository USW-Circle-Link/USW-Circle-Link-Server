package com.USWCicrcleLink.server.global.security.context;

import com.USWCicrcleLink.server.global.security.details.CustomLeaderDetails;
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
}
