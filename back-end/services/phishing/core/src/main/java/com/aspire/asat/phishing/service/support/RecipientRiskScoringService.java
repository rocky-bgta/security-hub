package com.aspire.asat.phishing.service.support;

import com.aspire.asat.phishing.dto.enums.ActivityType;
import com.aspire.asat.phishing.dto.enums.RiskLevel;
import com.aspire.asat.phishing.model.CampaignRecipient;
import com.aspire.asat.phishing.model.UserRiskProfile;
import com.aspire.asat.phishing.repository.CampaignRecipientRepository;
import com.aspire.asat.phishing.repository.UserRiskProfileRepository;
import com.aspire.asat.phishing.utils.RiskScoreUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecipientRiskScoringService {

    private final CampaignRecipientRepository recipientRepository;
    private final UserRiskProfileRepository userRiskProfileRepository;
    private final RegistrationRiskGroupSyncService registrationRiskGroupSyncService;

    @Value("${risk-score.training-weight}")
    private Double trainingWeight;

    @Value("${risk-score.phishing-weight}")
    private Double phishingWeight;

    public void updateUserRiskProfilePhishingScore(CampaignRecipient recipient, ActivityType activityType) {
        String clientId = recipient.getClientId();
        String userId = recipient.getUserId();
        if (clientId == null || clientId.isBlank() || userId == null || userId.isBlank()) {
            log.debug("Skipping UserRiskProfile update: missing clientId or userId");
            return;
        }
        try {
            List<CampaignRecipient> recipients = recipientRepository.findByUserId(userId);
            int count = recipients.size();
            double sum = 0.0;
            for (CampaignRecipient r : recipients) {
                sum += r.getRiskScore();
            }
            double average = count > 0 ? sum / count : 0.0;

            Optional<UserRiskProfile> existingOpt = userRiskProfileRepository.findByUserId(userId);
            RiskLevel previousLevel = existingOpt.map(UserRiskProfile::getRiskLevel).orElse(null);

            UserRiskProfile profile = existingOpt.orElseGet(() -> {
                UserRiskProfile p = new UserRiskProfile();
                p.setId(UUID.randomUUID().toString());
                p.setClientId(clientId);
                p.setUserId(userId);
                p.setEmail(recipient.getEmail());
                p.setFirstName(recipient.getFirstName());
                p.setLastName(recipient.getLastName());
                p.setDepartment(recipient.getDepartment());
                return p;
            });

            switch (activityType) {
                case EMAIL_SENT -> profile.setEmailsReceived(profile.getEmailsReceived() + 1);
                case EMAIL_OPENED -> profile.setEmailsOpened(profile.getEmailsOpened() + 1);
                case LINK_CLICKED -> profile.setLinksClicked(profile.getLinksClicked() + 1);
                case DATA_SUBMITTED -> profile.setDataSubmissions(profile.getDataSubmissions() + 1);
                case EMAIL_REPORTED -> profile.setEmailsReported(profile.getEmailsReported() + 1);
                case SMS_SENT -> { }
                case VOICE_INITIATED, VOICE_ANSWERED, VOICE_NO_ANSWER, VOICE_COMPROMISED, VOICE_FAILED,
                        VOICE_ENGAGED, VOICE_REPORTED -> { }
                default -> {
                    return;
                }
            }

            profile.setPhishingRiskScore(average);
            double training = profile.getTrainingRiskScore() != null ? profile.getTrainingRiskScore() : 0.0;
            double total = RiskScoreUtils.computeOverallRiskScore(training, average,
                    profile.getIsTrainingEnabled(), profile.getIsPhishingEnabled(), trainingWeight, phishingWeight);
            profile.setRiskScore(total);
            profile.setRiskLevelFromScore();
            profile.setUpdatedAt(Instant.now());
            userRiskProfileRepository.save(profile);

            registrationRiskGroupSyncService.syncIfNeeded(userId, previousLevel, profile.getRiskLevel());
            log.debug("Updated UserRiskProfile phishingRiskScore={}, riskScore={} for clientId={}, userId={}",
                    average, total, clientId, userId);
        } catch (Exception e) {
            log.error("Failed to update UserRiskProfile for clientId={}, userId={}: {}", clientId, userId, e.getMessage());
        }
    }
}
