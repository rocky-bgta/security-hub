package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.enums.RiskLevel;
import com.aspire.asat.phishing.model.UserRiskProfile;
import com.aspire.asat.phishing.repository.UserRiskProfileRepository;
import com.aspire.asat.phishing.service.support.RegistrationRiskGroupSyncService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingRiskScoreServiceImplTest {

    @Mock
    private UserRiskProfileRepository userRiskProfileRepository;
    @Mock
    private RegistrationRiskGroupSyncService registrationRiskGroupSyncService;

    @InjectMocks
    private TrainingRiskScoreServiceImpl trainingRiskScoreService;

    @BeforeEach
    void setUp() throws Exception {
        setField(trainingRiskScoreService, "trainingWeight", 0.25);
        setField(trainingRiskScoreService, "phishingWeight", 0.75);
    }

    @Test
    void updateTrainingRiskScore_firstCreate_syncsRegistration() {
        when(userRiskProfileRepository.findByClientIdAndUserId("client-1", "user-1"))
                .thenReturn(Optional.empty());
        when(userRiskProfileRepository.save(any(UserRiskProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        trainingRiskScoreService.updateTrainingRiskScore("user-1", "client-1", 0.0);

        ArgumentCaptor<UserRiskProfile> captor = ArgumentCaptor.forClass(UserRiskProfile.class);
        verify(userRiskProfileRepository).save(captor.capture());
        assertEquals(RiskLevel.LOW, captor.getValue().getRiskLevel());
        verify(registrationRiskGroupSyncService).syncIfNeeded(eq("user-1"), isNull(), eq(RiskLevel.LOW));
    }

    @Test
    void updateTrainingRiskScore_levelChanged_syncsNewLevel() {
        UserRiskProfile existing = new UserRiskProfile();
        existing.setId("p1");
        existing.setClientId("client-1");
        existing.setUserId("user-1");
        existing.setRiskLevel(RiskLevel.HIGH);
        existing.setRiskScore(70.0);
        existing.setTrainingRiskScore(100.0);
        existing.setIsTrainingEnabled(true);

        when(userRiskProfileRepository.findByClientIdAndUserId("client-1", "user-1"))
                .thenReturn(Optional.of(existing));
        when(userRiskProfileRepository.save(any(UserRiskProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        // Completed training → training risk 0 → LOW
        trainingRiskScoreService.updateTrainingRiskScore("user-1", "client-1", 0.0);

        ArgumentCaptor<UserRiskProfile> captor = ArgumentCaptor.forClass(UserRiskProfile.class);
        verify(userRiskProfileRepository).save(captor.capture());
        assertEquals(RiskLevel.LOW, captor.getValue().getRiskLevel());
        verify(registrationRiskGroupSyncService).syncIfNeeded(eq("user-1"), eq(RiskLevel.HIGH), eq(RiskLevel.LOW));
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
