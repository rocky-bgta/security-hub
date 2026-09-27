package com.aspire.asat.registration.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

/**
 * Implementation of AuditorAware for MongoDB auditing
 * Provides the current auditor (user) for @CreatedBy and @LastModifiedBy annotations
 */
public class AuditorAwareImpl implements AuditorAware<String> {
    private static final Logger logger = LoggerFactory.getLogger(AuditorAwareImpl.class);

    @Override
    public Optional<String> getCurrentAuditor() {
        // For now, return "system" as default
        // This can be enhanced to get the actual user from security context
        String username = "system";

        logger.debug("Auditing action by user: {}", username);
        return Optional.of(username);
    }
}

