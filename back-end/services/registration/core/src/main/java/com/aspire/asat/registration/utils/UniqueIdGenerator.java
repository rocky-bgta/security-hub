package com.aspire.asat.registration.utils;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UniqueIdGenerator {
    public String generateUUID() {
        return UUID.randomUUID().toString();
    }
}
