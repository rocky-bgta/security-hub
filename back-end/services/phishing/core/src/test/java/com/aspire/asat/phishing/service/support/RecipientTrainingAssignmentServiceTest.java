package com.aspire.asat.phishing.service.support;

import com.aspire.asat.phishing.client.RegistrationServiceClient;
import com.aspire.asat.phishing.dto.CampaignTrainingData;
import com.aspire.asat.phishing.dto.enums.CampaignStatus;
import com.aspire.asat.phishing.dto.enums.CampaignType;
import com.aspire.asat.phishing.dto.enums.RecipientStatus;
import com.aspire.asat.phishing.dto.enums.SubPackageAssignedFor;
import com.aspire.asat.phishing.model.Campaign;
import com.aspire.asat.phishing.model.CampaignRecipient;
import com.aspire.asat.phishing.model.CampaignStats;
import com.aspire.asat.phishing.repository.CampaignRecipientRepository;
import com.aspire.asat.phishing.repository.CampaignRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecipientTrainingAssignmentServiceTest {

    private static final String CAMPAIGN_ID = "campaign-1";
    private static final String RECIPIENT_ID = "recipient-1";

    @Mock
    private CampaignRepository campaignRepository;
    @Mock
    private CampaignRecipientRepository recipientRepository;
    @Mock
    private RegistrationServiceClient registrationServiceClient;

    @InjectMocks
    private RecipientTrainingAssignmentService recipientTrainingAssignmentService;

    private CampaignTrainingData trainingData;

    @BeforeEach
    void setUp() {
        trainingData = CampaignTrainingData.builder()
                .trainingModuleId("module-1")
                .name("Training")
                .productId("product-1")
                .packageId("package-1")
                .productPackageId("pp-1")
                .clientId("client-1")
                .assignedFor(SubPackageAssignedFor.PHISHING_WITH_TRAINING)
                .completionDays(CampaignTrainingData.CompletionDays.builder()
                        .durationUnit("DAYS")
                        .durationValue(7)
                        .build())
                .build();
    }

    @Test
    void skipsTrainingWhenCompletedTrainingCampaignNotExpired() {
        CampaignRecipient recipient = recipient();
        Campaign completedCampaign = trainingCampaign(CampaignStatus.COMPLETED, Instant.now().plusSeconds(3600));

        when(campaignRepository.findById(CAMPAIGN_ID)).thenReturn(Optional.of(completedCampaign));

        recipientTrainingAssignmentService.assignTrainingSubPackageIfNeeded(recipient, RecipientStatus.CLICKED);

        verify(registrationServiceClient, never()).assignSubPackageToUser(any());
        verify(recipientRepository, never()).save(any(CampaignRecipient.class));
    }

    @Test
    void skipsTrainingWhenCompletedTrainingCampaignExpired() {
        CampaignRecipient recipient = recipient();
        Campaign expiredCampaign = trainingCampaign(CampaignStatus.COMPLETED, Instant.now().minusSeconds(60));

        when(campaignRepository.findById(CAMPAIGN_ID)).thenReturn(Optional.of(expiredCampaign));

        recipientTrainingAssignmentService.assignTrainingSubPackageIfNeeded(recipient, RecipientStatus.CLICKED);

        verify(registrationServiceClient, never()).assignSubPackageToUser(any());
        verify(recipientRepository, never()).save(any(CampaignRecipient.class));
    }

    @Test
    void skipsTrainingWhenSimulatedPhishingCompleted() {
        CampaignRecipient recipient = recipient();
        Campaign campaign = Campaign.builder()
                .id(CAMPAIGN_ID)
                .campaignType(CampaignType.SIMULATED_PHISHING)
                .status(CampaignStatus.COMPLETED)
                .expiresAt(Instant.now().plusSeconds(3600))
                .trainingData(trainingData)
                .build();

        when(campaignRepository.findById(CAMPAIGN_ID)).thenReturn(Optional.of(campaign));

        recipientTrainingAssignmentService.assignTrainingSubPackageIfNeeded(recipient, RecipientStatus.CLICKED);

        verify(registrationServiceClient, never()).assignSubPackageToUser(any());
    }

    @Test
    void skipsTrainingWhenSmishingSimulation() {
        CampaignRecipient recipient = recipient();
        Campaign campaign = Campaign.builder()
                .id(CAMPAIGN_ID)
                .campaignType(CampaignType.SMISHING_SIMULATION)
                .status(CampaignStatus.RUNNING)
                .expiresAt(Instant.now().plusSeconds(3600))
                .trainingData(trainingData)
                .build();

        when(campaignRepository.findById(CAMPAIGN_ID)).thenReturn(Optional.of(campaign));

        recipientTrainingAssignmentService.assignTrainingSubPackageIfNeeded(recipient, RecipientStatus.CLICKED);

        verify(registrationServiceClient, never()).assignSubPackageToUser(any());
    }

    @Test
    void assignsTrainingWhenRunning() {
        CampaignRecipient recipient = recipient();
        Campaign runningCampaign = trainingCampaign(CampaignStatus.RUNNING, Instant.now().plusSeconds(3600));
        Campaign reloadedCampaign = trainingCampaign(CampaignStatus.RUNNING, Instant.now().plusSeconds(3600));
        reloadedCampaign.setStats(new CampaignStats());

        when(campaignRepository.findById(CAMPAIGN_ID)).thenReturn(Optional.of(runningCampaign),
                Optional.of(reloadedCampaign));

        recipientTrainingAssignmentService.assignTrainingSubPackageIfNeeded(recipient, RecipientStatus.CLICKED);

        verify(registrationServiceClient).assignSubPackageToUser(any());
        ArgumentCaptor<CampaignRecipient> recipientCaptor = ArgumentCaptor.forClass(CampaignRecipient.class);
        verify(recipientRepository).save(recipientCaptor.capture());
        assertTrue(recipientCaptor.getValue().isTrainingAssigned());
        verify(campaignRepository).save(reloadedCampaign);
    }

    private CampaignRecipient recipient() {
        return CampaignRecipient.builder()
                .id(RECIPIENT_ID)
                .campaignId(CAMPAIGN_ID)
                .userId("user-1")
                .trackingId("trk-1")
                .status(RecipientStatus.CLICKED)
                .build();
    }

    private Campaign trainingCampaign(CampaignStatus status, Instant expiresAt) {
        return Campaign.builder()
                .id(CAMPAIGN_ID)
                .campaignType(CampaignType.PHISHING_WITH_TRAINING)
                .status(status)
                .expiresAt(expiresAt)
                .trainingData(trainingData)
                .build();
    }
}
