package com.aspire.asat.phishing.service.support;

import com.aspire.asat.phishing.dto.enums.ActivityType;
import com.aspire.asat.phishing.dto.enums.RiskLevel;
import com.aspire.asat.phishing.model.CampaignRecipient;
import com.aspire.asat.phishing.model.UserRiskProfile;
import com.aspire.asat.phishing.repository.CampaignRecipientRepository;
import com.aspire.asat.phishing.repository.UserRiskProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecipientRiskScoringServiceTest {

    @Mock
    private CampaignRecipientRepository recipientRepository;
    @Mock
    private UserRiskProfileRepository userRiskProfileRepository;
    @Mock
    private RegistrationRiskGroupSyncService registrationRiskGroupSyncService;

    @InjectMocks
    private RecipientRiskScoringService scoringService;

    private CampaignRecipient recipient;

    @BeforeEach
    void setUp() throws Exception {
        setField(scoringService, "trainingWeight", 0.25);
        setField(scoringService, "phishingWeight", 0.75);

        recipient = CampaignRecipient.builder()
                .id("r1")
                .clientId("client-1")
                .userId("user-1")
                .email("u@example.com")
                .firstName("A")
                .lastName("B")
                .department("IT")
                .riskScore(10.0)
                .build();
    }

    @Test
    void firstTimeProfile_syncsRegistrationWithComputedLevel() {
        when(recipientRepository.findByUserId("user-1")).thenReturn(List.of(recipient));
        when(userRiskProfileRepository.findByUserId("user-1")).thenReturn(Optional.empty());
        when(userRiskProfileRepository.save(any(UserRiskProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        scoringService.updateUserRiskProfilePhishingScore(recipient, ActivityType.EMAIL_SENT);

        ArgumentCaptor<UserRiskProfile> profileCaptor = ArgumentCaptor.forClass(UserRiskProfile.class);
        verify(userRiskProfileRepository).save(profileCaptor.capture());
        UserRiskProfile saved = profileCaptor.getValue();
        assertEquals(RiskLevel.LOW, saved.getRiskLevel());
        verify(registrationRiskGroupSyncService).syncIfNeeded(eq("user-1"), isNull(), eq(RiskLevel.LOW));
    }

    @Test
    void existingProfile_levelUnchanged_stillInvokesSyncHelper() {
        UserRiskProfile existing = new UserRiskProfile();
        existing.setId("p1");
        existing.setClientId("client-1");
        existing.setUserId("user-1");
        existing.setRiskLevel(RiskLevel.LOW);
        existing.setRiskScore(10.0);
        existing.setPhishingRiskScore(10.0);
        existing.setIsPhishingEnabled(true);

        when(recipientRepository.findByUserId("user-1")).thenReturn(List.of(recipient));
        when(userRiskProfileRepository.findByUserId("user-1")).thenReturn(Optional.of(existing));
        when(userRiskProfileRepository.save(any(UserRiskProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        scoringService.updateUserRiskProfilePhishingScore(recipient, ActivityType.EMAIL_OPENED);

        verify(registrationRiskGroupSyncService).syncIfNeeded(eq("user-1"), eq(RiskLevel.LOW), eq(RiskLevel.LOW));
    }

    @Test
    void existingProfile_levelChanged_syncsNewLevel() {
        UserRiskProfile existing = new UserRiskProfile();
        existing.setId("p1");
        existing.setClientId("client-1");
        existing.setUserId("user-1");
        existing.setRiskLevel(RiskLevel.HIGH);
        existing.setRiskScore(60.0);
        existing.setPhishingRiskScore(60.0);
        existing.setIsPhishingEnabled(true);

        CampaignRecipient highRiskRecipient = CampaignRecipient.builder()
                .id("r2")
                .clientId("client-1")
                .userId("user-1")
                .email("u@example.com")
                .riskScore(100.0)
                .build();

        when(recipientRepository.findByUserId("user-1")).thenReturn(List.of(highRiskRecipient));
        when(userRiskProfileRepository.findByUserId("user-1")).thenReturn(Optional.of(existing));
        when(userRiskProfileRepository.save(any(UserRiskProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        scoringService.updateUserRiskProfilePhishingScore(highRiskRecipient, ActivityType.DATA_SUBMITTED);

        ArgumentCaptor<UserRiskProfile> profileCaptor = ArgumentCaptor.forClass(UserRiskProfile.class);
        verify(userRiskProfileRepository).save(profileCaptor.capture());
        verify(registrationRiskGroupSyncService).syncIfNeeded(
                eq("user-1"), eq(RiskLevel.HIGH), eq(profileCaptor.getValue().getRiskLevel()));
        verify(registrationRiskGroupSyncService, never()).syncAlways(any(), any());
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
