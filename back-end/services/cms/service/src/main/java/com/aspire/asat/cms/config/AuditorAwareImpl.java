package com.aspire.asat.cms.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

public class AuditorAwareImpl implements AuditorAware<String> {
    private static final Logger logger = LoggerFactory.getLogger(AuditorAwareImpl.class);
    @Override
    public Optional<String> getCurrentAuditor() {
        String username = "system";
        //username = SecurityContextHolder.getContext().getAuthentication().getName();
        logger.info("Auditing action by user: {}", username);
        return Optional.of(username);
    }

}
