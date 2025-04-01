package com.USWCicrcleLink.server.global.util;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RandomUUIDProvider implements UUIDProvider {

    @Override
    public UUID generate() {
        return UUID.randomUUID();
    }
}
