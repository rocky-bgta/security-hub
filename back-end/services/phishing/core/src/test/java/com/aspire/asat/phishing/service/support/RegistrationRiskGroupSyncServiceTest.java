package com.aspire.asat.phishing.service.support;

import com.aspire.asat.phishing.client.RegistrationServiceClient;
import com.aspire.asat.phishing.dto.enums.RiskLevel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class RegistrationRiskGroupSyncServiceTest {

    @Mock
    private RegistrationServiceClient registrationServiceClient;

    @InjectMocks
    private RegistrationRiskGroupSyncService syncService;

    @Test
    void syncIfNeeded_firstCreate_pushesNewLevel() {
        syncService.syncIfNeeded("user-1", null, RiskLevel.LOW);

        verify(registrationServiceClient).updateUserRiskGroup("user-1", RiskLevel.LOW);
    }

    @Test
    void syncIfNeeded_levelChanged_pushesNewLevel() {
        syncService.syncIfNeeded("user-1", RiskLevel.HIGH, RiskLevel.MEDIUM);

        verify(registrationServiceClient).updateUserRiskGroup("user-1", RiskLevel.MEDIUM);
    }

    @Test
    void syncIfNeeded_levelUnchanged_skips() {
        syncService.syncIfNeeded("user-1", RiskLevel.LOW, RiskLevel.LOW);

        verify(registrationServiceClient, never()).updateUserRiskGroup(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void syncIfNeeded_blankUserId_skips() {
        syncService.syncIfNeeded("  ", null, RiskLevel.LOW);

        verifyNoInteractions(registrationServiceClient);
    }

    @Test
    void syncIfNeeded_nullNewLevel_skips() {
        syncService.syncIfNeeded("user-1", RiskLevel.HIGH, null);

        verifyNoInteractions(registrationServiceClient);
    }

    @Test
    void syncIfNeeded_clientFailure_swallowsException() {
        doThrow(new RuntimeException("registration down"))
                .when(registrationServiceClient).updateUserRiskGroup("user-1", RiskLevel.LOW);

        syncService.syncIfNeeded("user-1", null, RiskLevel.LOW);

        verify(registrationServiceClient).updateUserRiskGroup("user-1", RiskLevel.LOW);
    }

    @Test
    void syncAlways_pushesEvenWhenUnchanged() {
        syncService.syncAlways("user-1", RiskLevel.LOW);

        verify(registrationServiceClient).updateUserRiskGroup("user-1", RiskLevel.LOW);
    }
}
