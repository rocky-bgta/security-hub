package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.enums.CampaignStatus;
import com.aspire.asat.phishing.dto.enums.CampaignType;
import com.aspire.asat.phishing.dto.enums.RecipientStatus;
import com.aspire.asat.phishing.model.Campaign;
import com.aspire.asat.phishing.model.CampaignStats;
import com.aspire.asat.phishing.repository.CampaignRecipientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CampaignCompletionEvaluatorTest {

    private static final String CAMPAIGN_ID = "campaign-1";

    @Mock
    private CampaignRecipientRepository recipientRepository;

    @InjectMocks
    private CampaignCompletionEvaluator evaluator;

    private Campaign runningCampaign(CampaignType type) {
        return Campaign.builder()
                .id(CAMPAIGN_ID)
                .clientId("client-1")
                .campaignName("c")
                .campaignType(type)
                .status(CampaignStatus.RUNNING)
                .expiresAt(Instant.now().plusSeconds(3600))
                .stats(new CampaignStats())
                .build();
    }

    @Test
    void simulatedCompletesWhenAllRecipientsEngaged() {
        Campaign campaign = runningCampaign(CampaignType.SIMULATED_PHISHING);
        when(recipientRepository.countByCampaignId(CAMPAIGN_ID)).thenReturn(3L);
        when(recipientRepository.countByCampaignIdAndStatusIn(eq(CAMPAIGN_ID), any())).thenReturn(0L);

        boolean changed = evaluator.evaluateAndApply(campaign);

        assertTrue(changed);
        assertEquals(CampaignStatus.COMPLETED, campaign.getStatus());
        assertNotNull(campaign.getCompletedAt());
        assertNull(campaign.getTrainingCompletedAt());
    }

    @Test
    void simulatedStaysRunningWhenEmailsSentButNotEngaged() {
        Campaign campaign = runningCampaign(CampaignType.SIMULATED_PHISHING);
        when(recipientRepository.countByCampaignId(CAMPAIGN_ID)).thenReturn(2L);
        when(recipientRepository.countByCampaignIdAndStatusIn(eq(CAMPAIGN_ID), any())).thenReturn(2L);

        boolean changed = evaluator.evaluateAndApply(campaign);

        assertFalse(changed);
        assertEquals(CampaignStatus.RUNNING, campaign.getStatus());
    }

    @Test
    void simulatedCompletesWhenEngagedAndBounced() {
        Campaign campaign = runningCampaign(CampaignType.SIMULATED_PHISHING);
        when(recipientRepository.countByCampaignId(CAMPAIGN_ID)).thenReturn(4L);
        when(recipientRepository.countByCampaignIdAndStatusIn(eq(CAMPAIGN_ID), any())).thenReturn(0L);

        boolean changed = evaluator.evaluateAndApply(campaign);

        assertTrue(changed);
        assertEquals(CampaignStatus.COMPLETED, campaign.getStatus());
    }

    @Test
    void simulatedCompletesWhenAllBounced() {
        Campaign campaign = runningCampaign(CampaignType.SIMULATED_PHISHING);
        when(recipientRepository.countByCampaignId(CAMPAIGN_ID)).thenReturn(2L);
        when(recipientRepository.countByCampaignIdAndStatusIn(eq(CAMPAIGN_ID), any())).thenReturn(0L);

        boolean changed = evaluator.evaluateAndApply(campaign);

        assertTrue(changed);
        assertEquals(CampaignStatus.COMPLETED, campaign.getStatus());
    }

    @Test
    void simulatedStaysRunningWhilePending() {
        Campaign campaign = runningCampaign(CampaignType.SIMULATED_PHISHING);
        when(recipientRepository.countByCampaignId(CAMPAIGN_ID)).thenReturn(3L);
        when(recipientRepository.countByCampaignIdAndStatusIn(eq(CAMPAIGN_ID), any())).thenReturn(1L);

        boolean changed = evaluator.evaluateAndApply(campaign);

        assertFalse(changed);
        assertEquals(CampaignStatus.RUNNING, campaign.getStatus());
    }

    @Test
    void simulatedDoesNotCompleteWithZeroRecipients() {
        Campaign campaign = runningCampaign(CampaignType.SIMULATED_PHISHING);
        when(recipientRepository.countByCampaignId(CAMPAIGN_ID)).thenReturn(0L);

        boolean changed = evaluator.evaluateAndApply(campaign);

        assertFalse(changed);
        assertEquals(CampaignStatus.RUNNING, campaign.getStatus());
    }

    @Test
    void simulatedStaysRunningWhenEmailsPending() {
        Campaign campaign = runningCampaign(CampaignType.SIMULATED_PHISHING);
        when(recipientRepository.countByCampaignId(CAMPAIGN_ID)).thenReturn(3L);
        when(recipientRepository.countByCampaignIdAndStatusIn(eq(CAMPAIGN_ID), any())).thenReturn(3L);

        boolean changed = evaluator.evaluateAndApply(campaign);

        assertFalse(changed);
        assertEquals(CampaignStatus.RUNNING, campaign.getStatus());
    }

    @Test
    void trainingCompletesWhenAllAssignedTrainingsComplete() {
        Campaign campaign = runningCampaign(CampaignType.PHISHING_WITH_TRAINING);
        when(recipientRepository.countByCampaignIdAndStatus(CAMPAIGN_ID, RecipientStatus.PENDING)).thenReturn(0L);
        when(recipientRepository.countByCampaignIdAndTrainingAssignedTrue(CAMPAIGN_ID)).thenReturn(3L);
        when(recipientRepository.countByCampaignIdAndTrainingAssignedTrueAndTrainingCompleted(CAMPAIGN_ID))
                .thenReturn(3L);

        boolean changed = evaluator.evaluateAndApply(campaign);

        assertTrue(changed);
        assertEquals(CampaignStatus.COMPLETED, campaign.getStatus());
        assertNotNull(campaign.getCompletedAt());
        assertNotNull(campaign.getTrainingCompletedAt());
    }

    @Test
    void trainingStaysRunningWhenNoTrainingAssigned() {
        Campaign campaign = runningCampaign(CampaignType.PHISHING_WITH_TRAINING);
        when(recipientRepository.countByCampaignIdAndStatus(CAMPAIGN_ID, RecipientStatus.PENDING)).thenReturn(0L);
        when(recipientRepository.countByCampaignIdAndTrainingAssignedTrue(CAMPAIGN_ID)).thenReturn(0L);

        boolean changed = evaluator.evaluateAndApply(campaign);

        assertFalse(changed);
        assertEquals(CampaignStatus.RUNNING, campaign.getStatus());
        assertNull(campaign.getCompletedAt());
    }

    @Test
    void trainingStaysRunningWhenPartialTrainingIncomplete() {
        Campaign campaign = runningCampaign(CampaignType.PHISHING_WITH_TRAINING);
        when(recipientRepository.countByCampaignIdAndStatus(CAMPAIGN_ID, RecipientStatus.PENDING)).thenReturn(0L);
        when(recipientRepository.countByCampaignIdAndTrainingAssignedTrue(CAMPAIGN_ID)).thenReturn(3L);
        when(recipientRepository.countByCampaignIdAndTrainingAssignedTrueAndTrainingCompleted(CAMPAIGN_ID))
                .thenReturn(2L);

        boolean changed = evaluator.evaluateAndApply(campaign);

        assertFalse(changed);
        assertEquals(CampaignStatus.RUNNING, campaign.getStatus());
    }

    @Test
    void trainingCompletesWhenAllNonBouncedAssignedComplete() {
        Campaign campaign = runningCampaign(CampaignType.PHISHING_WITH_TRAINING);
        when(recipientRepository.countByCampaignIdAndStatus(CAMPAIGN_ID, RecipientStatus.PENDING)).thenReturn(0L);
        when(recipientRepository.countByCampaignIdAndTrainingAssignedTrue(CAMPAIGN_ID)).thenReturn(2L);
        when(recipientRepository.countByCampaignIdAndTrainingAssignedTrueAndTrainingCompleted(CAMPAIGN_ID))
                .thenReturn(2L);

        boolean changed = evaluator.evaluateAndApply(campaign);

        assertTrue(changed);
        assertEquals(CampaignStatus.COMPLETED, campaign.getStatus());
        assertNotNull(campaign.getCompletedAt());
        assertNotNull(campaign.getTrainingCompletedAt());
    }

    @Test
    void trainingCompletesWhenAllAssignedHavePhishingTrainingCompletedStatus() {
        Campaign campaign = runningCampaign(CampaignType.PHISHING_WITH_TRAINING);
        when(recipientRepository.countByCampaignIdAndStatus(CAMPAIGN_ID, RecipientStatus.PENDING)).thenReturn(0L);
        when(recipientRepository.countByCampaignIdAndTrainingAssignedTrue(CAMPAIGN_ID)).thenReturn(2L);
        when(recipientRepository.countByCampaignIdAndTrainingAssignedTrueAndTrainingCompleted(CAMPAIGN_ID))
                .thenReturn(2L);

        boolean changed = evaluator.evaluateAndApply(campaign);

        assertTrue(changed);
        assertEquals(CampaignStatus.COMPLETED, campaign.getStatus());
    }

    @Test
    void smishingSimulationCompletesWhenAllRecipientsEngaged() {
        Campaign campaign = runningCampaign(CampaignType.SMISHING_SIMULATION);
        when(recipientRepository.countByCampaignId(CAMPAIGN_ID)).thenReturn(3L);
        when(recipientRepository.countByCampaignIdAndStatusIn(eq(CAMPAIGN_ID), any())).thenReturn(0L);

        boolean changed = evaluator.evaluateAndApply(campaign);

        assertTrue(changed);
        assertEquals(CampaignStatus.COMPLETED, campaign.getStatus());
        assertNotNull(campaign.getCompletedAt());
        assertNull(campaign.getTrainingCompletedAt());
    }

    @Test
    void smishingSimulationStaysRunningWhenSmsSentButNotEngaged() {
        Campaign campaign = runningCampaign(CampaignType.SMISHING_SIMULATION);
        when(recipientRepository.countByCampaignId(CAMPAIGN_ID)).thenReturn(2L);
        when(recipientRepository.countByCampaignIdAndStatusIn(eq(CAMPAIGN_ID), any())).thenReturn(2L);

        boolean changed = evaluator.evaluateAndApply(campaign);

        assertFalse(changed);
        assertEquals(CampaignStatus.RUNNING, campaign.getStatus());
    }

    @Test
    void smishingSimulationDoesNotCompleteWithZeroRecipients() {
        Campaign campaign = runningCampaign(CampaignType.SMISHING_SIMULATION);
        when(recipientRepository.countByCampaignId(CAMPAIGN_ID)).thenReturn(0L);

        boolean changed = evaluator.evaluateAndApply(campaign);

        assertFalse(changed);
        assertEquals(CampaignStatus.RUNNING, campaign.getStatus());
    }

    @Test
    void smishingWithTrainingCompletesWhenAllAssignedTrainingsComplete() {
        Campaign campaign = runningCampaign(CampaignType.SMISHING_WITH_TRAINING);
        when(recipientRepository.countByCampaignIdAndStatus(CAMPAIGN_ID, RecipientStatus.PENDING)).thenReturn(0L);
        when(recipientRepository.countByCampaignIdAndTrainingAssignedTrue(CAMPAIGN_ID)).thenReturn(3L);
        when(recipientRepository.countByCampaignIdAndTrainingAssignedTrueAndTrainingCompleted(CAMPAIGN_ID))
                .thenReturn(3L);

        boolean changed = evaluator.evaluateAndApply(campaign);

        assertTrue(changed);
        assertEquals(CampaignStatus.COMPLETED, campaign.getStatus());
        assertNotNull(campaign.getCompletedAt());
        assertNotNull(campaign.getTrainingCompletedAt());
    }

    @Test
    void smishingWithTrainingStaysRunningWhenSmsStillPending() {
        Campaign campaign = runningCampaign(CampaignType.SMISHING_WITH_TRAINING);
        when(recipientRepository.countByCampaignIdAndStatus(CAMPAIGN_ID, RecipientStatus.PENDING)).thenReturn(1L);

        boolean changed = evaluator.evaluateAndApply(campaign);

        assertFalse(changed);
        assertEquals(CampaignStatus.RUNNING, campaign.getStatus());
    }

    @Test
    void smishingWithTrainingStaysRunningWhenNoTrainingAssigned() {
        Campaign campaign = runningCampaign(CampaignType.SMISHING_WITH_TRAINING);
        when(recipientRepository.countByCampaignIdAndStatus(CAMPAIGN_ID, RecipientStatus.PENDING)).thenReturn(0L);
        when(recipientRepository.countByCampaignIdAndTrainingAssignedTrue(CAMPAIGN_ID)).thenReturn(0L);

        boolean changed = evaluator.evaluateAndApply(campaign);

        assertFalse(changed);
        assertEquals(CampaignStatus.RUNNING, campaign.getStatus());
    }

    @Test
    void smishingWithTrainingStaysRunningWhenPartialTrainingIncomplete() {
        Campaign campaign = runningCampaign(CampaignType.SMISHING_WITH_TRAINING);
        when(recipientRepository.countByCampaignIdAndStatus(CAMPAIGN_ID, RecipientStatus.PENDING)).thenReturn(0L);
        when(recipientRepository.countByCampaignIdAndTrainingAssignedTrue(CAMPAIGN_ID)).thenReturn(3L);
        when(recipientRepository.countByCampaignIdAndTrainingAssignedTrueAndTrainingCompleted(CAMPAIGN_ID))
                .thenReturn(2L);

        boolean changed = evaluator.evaluateAndApply(campaign);

        assertFalse(changed);
        assertEquals(CampaignStatus.RUNNING, campaign.getStatus());
    }

    @Test
    void expiredCampaignIsNeverCompleted() {
        Campaign campaign = runningCampaign(CampaignType.SIMULATED_PHISHING);
        campaign.setExpiresAt(Instant.now().minusSeconds(60));

        boolean changed = evaluator.evaluateAndApply(campaign);

        assertFalse(changed);
        assertEquals(CampaignStatus.RUNNING, campaign.getStatus());
    }

    @Test
    void nonRunningCampaignIsIgnored() {
        Campaign campaign = runningCampaign(CampaignType.SIMULATED_PHISHING);
        campaign.setStatus(CampaignStatus.DRAFT);

        boolean changed = evaluator.evaluateAndApply(campaign);

        assertFalse(changed);
        assertEquals(CampaignStatus.DRAFT, campaign.getStatus());
    }
}
