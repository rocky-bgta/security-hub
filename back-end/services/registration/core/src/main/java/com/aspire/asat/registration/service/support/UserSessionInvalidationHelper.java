package com.aspire.asat.registration.service.support;

import com.aspire.asat.registration.client.service.AuthServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Forces auth logout when a user status becomes restrictive (INACTIVE / SUSPEND / blocked).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UserSessionInvalidationHelper {

    private static final Set<String> RESTRICTIVE_STATUSES = Set.of(
            "INACTIVE",
            "SUSPEND",
            "SUSPENDED",
            "TEMPORARY_BLOCKED",
            "BLOCKED",
            "BLOCK"
    );

    private final AuthServiceClient authServiceClient;

    public boolean isRestrictiveStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return false;
        }
        return RESTRICTIVE_STATUSES.contains(status.trim().toUpperCase(Locale.ROOT));
    }

    /**
     * Calls Auth logout for each distinct userId when status is restrictive.
     * Best-effort: failures are logged inside AuthServiceClient and do not abort the caller.
     */
    public void logoutUsersIfRestrictive(String status, Collection<String> userIds) {
        if (!isRestrictiveStatus(status) || userIds == null || userIds.isEmpty()) {
            return;
        }

        Set<String> distinctIds = new LinkedHashSet<>();
        for (String userId : userIds) {
            if (StringUtils.hasText(userId)) {
                distinctIds.add(userId.trim());
            }
        }

        if (distinctIds.isEmpty()) {
            return;
        }

        log.info("Forcing logout for {} user(s) due to status change to {}", distinctIds.size(), status);
        for (String userId : distinctIds) {
            authServiceClient.logoutUser(userId);
        }
    }
}
