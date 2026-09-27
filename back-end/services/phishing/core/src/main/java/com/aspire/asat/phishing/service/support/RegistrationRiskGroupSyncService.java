package com.aspire.asat.phishing.service.support;

import com.aspire.asat.phishing.client.RegistrationServiceClient;
import com.aspire.asat.phishing.dto.enums.RiskLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Syncs phishing {@link RiskLevel} to registration {@code AspireUser.riskGroup}.
 * Centralizes first-create and level-change sync so every risk writer uses one path.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RegistrationRiskGroupSyncService {

    private final RegistrationServiceClient registrationServiceClient;

    /**
     * Syncs registration when the profile is new ({@code previousLevel == null})
     * or when the risk band changes. No-ops when the level is unchanged.
     */
    public void syncIfNeeded(String userId, RiskLevel previousLevel, RiskLevel newLevel) {
        if (userId == null || userId.isBlank() || newLevel == null) {
            return;
        }
        if (previousLevel != null && previousLevel.equals(newLevel)) {
            return;
        }
        pushToRegistration(userId, newLevel);
    }

    /**
     * Always pushes the current phishing risk level to registration.
     * Intended for admin recalculate / backfill of drifted {@code riskGroup} values.
     */
    public void syncAlways(String userId, RiskLevel newLevel) {
        if (userId == null || userId.isBlank() || newLevel == null) {
            return;
        }
        pushToRegistration(userId, newLevel);
    }

    private void pushToRegistration(String userId, RiskLevel newLevel) {
        try {
            registrationServiceClient.updateUserRiskGroup(userId, newLevel);
        } catch (Exception e) {
            log.warn("Failed to sync riskGroup to registration for userId={}: {}", userId, e.getMessage());
        }
    }
}
