package com.USWCicrcleLink.server.global.security.context;

import com.USWCicrcleLink.server.user.domain.User;

import java.util.UUID;

public interface AuthContext {
    UUID getCurrentUUID();
    User getUserByAuth();
}
