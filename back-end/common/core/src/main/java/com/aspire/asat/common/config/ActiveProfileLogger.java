package com.aspire.asat.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Logs the active Spring profile(s) when the application has started.
 * Applied to all services that scan {@code com.aspire.asat}.
 */
@Component
public class ActiveProfileLogger implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger log = LoggerFactory.getLogger(ActiveProfileLogger.class);

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        Environment env = event.getApplicationContext().getEnvironment();
        String appName = env.getProperty("spring.application.name", "application");
        String[] activeProfiles = env.getActiveProfiles();
        String profiles = activeProfiles.length > 0 ? String.join(", ", activeProfiles) : "(none)";
        log.info("Application [{}] started with active profile(s): {}", appName, profiles);
    }
}
