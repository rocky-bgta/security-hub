package com.aspire.asat.phishing.scheduler;

import com.aspire.asat.phishing.client.CmsPhishingCourseClient;
import com.aspire.asat.phishing.dto.CampaignTrainingData;
import com.aspire.asat.phishing.dto.cms.CmsPhishingCourseEnrollmentDto;
import com.aspire.asat.phishing.dto.cms.CmsPhishingCoursePageDto;
import com.aspire.asat.phishing.dto.enums.CampaignStatus;
import com.aspire.asat.phishing.dto.enums.CampaignType;
import com.aspire.asat.phishing.model.Campaign;
import com.aspire.asat.phishing.model.CampaignRecipient;
import com.aspire.asat.phishing.model.CampaignStats;
import com.aspire.asat.phishing.repository.CampaignRecipientRepository;
import com.aspire.asat.phishing.repository.CampaignRepository;
import com.aspire.asat.phishing.service.impl.CampaignCompletionEvaluator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CampaignTrainingCompletionSchedulerTest {

    private static final String CLIENT_ID = "client-1";
    private static final String CAMPAIGN_ID = "campaign-1";
    private static final String MODULE_ID = "sub-package-1";

    @Mock
    private CampaignRepository campaignRepository;
    @Mock
    private CampaignRecipientRepository recipientRepository;
    @Mock
    private CmsPhishingCourseClient cmsPhishingCourseClient;
    @Mock
    private CampaignCompletionEvaluator completionEvaluator;

    @InjectMocks
    private CampaignTrainingCompletionScheduler scheduler;

    private Campaign trainingCampaign(Instant expiresAt) {
        return trainingCampaign(expiresAt, CampaignType.PHISHING_WITH_TRAINING);
    }

    private Campaign trainingCampaign(Instant expiresAt, CampaignType type) {
        return Campaign.builder()
                .id(CAMPAIGN_ID)
                .clientId(CLIENT_ID)
                .campaignName("c")
                .campaignType(type)
                .status(CampaignStatus.RUNNING)
                .expiresAt(expiresAt)
                .stats(new CampaignStats())
                .trainingData(CampaignTrainingData.builder().trainingModuleId(MODULE_ID).build())
                .build();
    }

    private CampaignRecipient assignedRecipient(String userId) {
        return CampaignRecipient.builder()
                .id("r-" + userId)
                .campaignId(CAMPAIGN_ID)
                .clientId(CLIENT_ID)
                .userId(userId)
                .trainingAssigned(true)
                .build();
    }

    @Test
    void completesCampaignWhenAllAssignedTrainingsCompleteInCms() {
        Campaign campaign = trainingCampaign(Instant.now().plusSeconds(3600));
        when(campaignRepository.findByStatusAndCampaignTypeIn(eq(CampaignStatus.RUNNING), any()))
                .thenReturn(List.of(campaign));

        List<CampaignRecipient> assigned = List.of(assignedRecipient("u1"), assignedRecipient("u2"));
        when(recipientRepository.findByCampaignIdAndTrainingAssignedTrue(CAMPAIGN_ID)).thenReturn(assigned);

        CmsPhishingCoursePageDto page = CmsPhishingCoursePageDto.builder()
                .offset(0).pageSize(500).total(2L)
                .items(List.of(
                        CmsPhishingCourseEnrollmentDto.builder().userId("u1").subPackageId(MODULE_ID).status("complete").build(),
                        CmsPhishingCourseEnrollmentDto.builder().userId("u2").subPackageId(MODULE_ID).status("complete").build()))
                .build();
        when(cmsPhishingCourseClient.getDetails(eq(CLIENT_ID), anyInt(), anyInt(), any())).thenReturn(page);
        when(completionEvaluator.evaluateAndApply(campaign)).thenReturn(true);

        scheduler.sweepTrainingCampaigns();

        ArgumentCaptor<List<CampaignRecipient>> captor = ArgumentCaptor.forClass(List.class);
        verify(recipientRepository).saveAll(captor.capture());
        List<CampaignRecipient> saved = captor.getValue();
        assertEquals(2, saved.size());
        assertTrue(saved.stream().allMatch(CampaignRecipient::isTrainingCompleted));

        assertEquals(2, campaign.getStats().getTrainingCompletedCount());
        assertEquals(2, campaign.getStats().getTrainingAssignedCount());
        verify(campaignRepository).save(campaign);
    }

    @Test
    void skipsExpiredCampaign() {
        Campaign expired = trainingCampaign(Instant.now().minusSeconds(60));
        when(campaignRepository.findByStatusAndCampaignTypeIn(eq(CampaignStatus.RUNNING), any()))
                .thenReturn(List.of(expired));

        scheduler.sweepTrainingCampaigns();

        verify(recipientRepository, never()).findByCampaignIdAndTrainingAssignedTrue(any());
        verify(cmsPhishingCourseClient, never()).getDetails(any(), anyInt(), anyInt(), any());
        verify(completionEvaluator, never()).evaluateAndApply(any());
        verify(campaignRepository, never()).save(any());
    }

    @Test
    void recognizesPhishingTrainingCompletedEnrollmentFromCms() {
        Campaign campaign = trainingCampaign(Instant.now().plusSeconds(3600));
        when(campaignRepository.findByStatusAndCampaignTypeIn(eq(CampaignStatus.RUNNING), any()))
                .thenReturn(List.of(campaign));

        List<CampaignRecipient> assigned = List.of(assignedRecipient("u1"));
        when(recipientRepository.findByCampaignIdAndTrainingAssignedTrue(CAMPAIGN_ID)).thenReturn(assigned);

        CmsPhishingCoursePageDto page = CmsPhishingCoursePageDto.builder()
                .offset(0).pageSize(500).total(1L)
                .items(List.of(
                        CmsPhishingCourseEnrollmentDto.builder()
                                .userId("u1")
                                .subPackageId(MODULE_ID)
                                .status("PHISHING_TRAINING_COMPLETED")
                                .build()))
                .build();
        when(cmsPhishingCourseClient.getDetails(eq(CLIENT_ID), anyInt(), anyInt(), any())).thenReturn(page);
        when(completionEvaluator.evaluateAndApply(campaign)).thenReturn(true);

        scheduler.sweepTrainingCampaigns();

        ArgumentCaptor<List<CampaignRecipient>> captor = ArgumentCaptor.forClass(List.class);
        verify(recipientRepository).saveAll(captor.capture());
        assertTrue(captor.getValue().get(0).isTrainingCompleted());
        assertEquals("PHISHING_TRAINING_COMPLETED", captor.getValue().get(0).getTrainingStatus());
    }

    @Test
    void doesNothingWhenNoRunningTrainingCampaigns() {
        when(campaignRepository.findByStatusAndCampaignTypeIn(eq(CampaignStatus.RUNNING), any()))
                .thenReturn(List.of());

        scheduler.sweepTrainingCampaigns();

        verify(recipientRepository, never()).findByCampaignIdAndTrainingAssignedTrue(any());
    }

    @Test
    void sweepQueriesBothPhishingAndSmishingTrainingTypes() {
        when(campaignRepository.findByStatusAndCampaignTypeIn(eq(CampaignStatus.RUNNING), any()))
                .thenReturn(List.of());

        scheduler.sweepTrainingCampaigns();

        ArgumentCaptor<Collection<CampaignType>> typesCaptor = ArgumentCaptor.forClass(Collection.class);
        verify(campaignRepository).findByStatusAndCampaignTypeIn(eq(CampaignStatus.RUNNING), typesCaptor.capture());
        assertTrue(typesCaptor.getValue().contains(CampaignType.PHISHING_WITH_TRAINING));
        assertTrue(typesCaptor.getValue().contains(CampaignType.SMISHING_WITH_TRAINING));
    }

    @Test
    void completesSmishingTrainingCampaignWhenAllAssignedTrainingsCompleteInCms() {
        Campaign campaign = trainingCampaign(Instant.now().plusSeconds(3600), CampaignType.SMISHING_WITH_TRAINING);
        when(campaignRepository.findByStatusAndCampaignTypeIn(eq(CampaignStatus.RUNNING), any()))
                .thenReturn(List.of(campaign));

        List<CampaignRecipient> assigned = List.of(assignedRecipient("u1"), assignedRecipient("u2"));
        when(recipientRepository.findByCampaignIdAndTrainingAssignedTrue(CAMPAIGN_ID)).thenReturn(assigned);

        CmsPhishingCoursePageDto page = CmsPhishingCoursePageDto.builder()
                .offset(0).pageSize(500).total(2L)
                .items(List.of(
                        CmsPhishingCourseEnrollmentDto.builder().userId("u1").subPackageId(MODULE_ID).status("complete").build(),
                        CmsPhishingCourseEnrollmentDto.builder().userId("u2").subPackageId(MODULE_ID).status("complete").build()))
                .build();
        when(cmsPhishingCourseClient.getDetails(eq(CLIENT_ID), anyInt(), anyInt(), any())).thenReturn(page);
        when(completionEvaluator.evaluateAndApply(campaign)).thenReturn(true);

        scheduler.sweepTrainingCampaigns();

        verify(completionEvaluator).evaluateAndApply(campaign);
        verify(campaignRepository).save(campaign);
        assertEquals(2, campaign.getStats().getTrainingCompletedCount());
        assertEquals(2, campaign.getStats().getTrainingAssignedCount());
    }
}
